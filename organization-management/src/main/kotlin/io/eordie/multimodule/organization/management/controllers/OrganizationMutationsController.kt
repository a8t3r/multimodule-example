package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.repository.EntityConverter.copyFields
import io.eordie.multimodule.common.security.context.getAuthentication
import io.eordie.multimodule.common.validation.singleOrError
import io.eordie.multimodule.contracts.basic.BasePermission
import io.eordie.multimodule.contracts.organization.OrganizationInput
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.extra.OrganizationPublicInformation
import io.eordie.multimodule.contracts.organization.models.extra.OrganizationPublicInformationFilter
import io.eordie.multimodule.contracts.organization.services.OrganizationMutations
import io.eordie.multimodule.contracts.organization.services.SuggestionQueries
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.OrganizationPublicInformationModelDraft
import io.eordie.multimodule.organization.management.repository.OrganizationFactory
import io.eordie.multimodule.organization.management.repository.OrganizationPublicInformationFactory
import io.phasetwo.client.OrganizationsResource
import io.phasetwo.client.openapi.model.OrganizationRepresentation
import jakarta.inject.Singleton
import java.util.*

@Singleton
class OrganizationMutationsController(
    private val suggestions: SuggestionQueries,
    private val resource: OrganizationsResource,
    private val organizations: OrganizationFactory,
    private val information: OrganizationPublicInformationFactory
) : OrganizationMutations {

    private fun OrganizationInput.toRepresentation() = OrganizationRepresentation()
        .name(this.name)
        .displayName(this.displayName)

    override suspend fun organization(input: OrganizationInput): Organization = organization(input, null)

    private suspend fun savePublicInformation(organizationId: UUID, info: OrganizationPublicInformation) {
        information.save<OrganizationPublicInformationModelDraft>(organizationId) { _, instance ->
            instance.copyFields(info::name, info::address, info::inn, info::kpp, info::ogrn)
            instance.organizationId = organizationId
            info.location?.let {
                instance.locationLon = it.x
                instance.locationLat = it.y
            }
        }
    }

    private suspend fun organization(
        input: OrganizationInput,
        info: OrganizationPublicInformation?
    ): Organization {
        val userId = getAuthentication().userId
        val organization = organizations.findByInput(input)
        val organizationId = if (organization == null) {
            // perform creation
            val rawOrganizationId = resource.create(input.toRepresentation())
            val organizationResource = resource.organization(rawOrganizationId)
            organizationResource.memberships().add(userId.toString())
            val rolesResource = organizationResource.roles()
            Roles.entries.filter { it.isOrganizationRole() }.forEach {
                rolesResource.grant(it.internalName(), userId.toString())
            }

            val organizationId = UUID.fromString(rawOrganizationId)
            organizations.changeCreatedBy(organizationId, userId)

            val filter = OrganizationPublicInformationFilter(query = input.name)
            (info ?: suggestions.suggestions(filter).singleOrNull())?.let { savePublicInformation(organizationId, it) }

            organizationId
        } else {
            // perform update
            organizations.checkPermission(organization, BasePermission.MANAGE)
            resource.organization(organization.id.toString()).update(input.toRepresentation())
            organization.id
        }

        return organizations.getById(organizationId).convert()
    }

    override suspend fun organizationByVat(vat: String): Organization {
        val info = suggestions.suggestions(OrganizationPublicInformationFilter(vat = vat)).singleOrError()
        return organization(OrganizationInput(id = null, name = info.name), info)
    }

    override suspend fun deleteOrganization(organizationId: UUID) {
        val organization = organizations.getById(organizationId)
        organizations.checkPermission(organization, BasePermission.PURGE)
        resource.organization(organization.id.toString()).delete()
        // no event should be produced because tuple was deleted at previous stage
        organizations.deleteById(organization.id)
    }
}
