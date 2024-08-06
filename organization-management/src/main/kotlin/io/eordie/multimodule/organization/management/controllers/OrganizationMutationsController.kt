package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.security.context.getAuthentication
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.organization.OrganizationInput
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.services.OrganizationMutations
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.repository.OrganizationFactory
import io.phasetwo.client.OrganizationsResource
import io.phasetwo.client.openapi.model.OrganizationRepresentation
import jakarta.inject.Singleton
import java.util.*

@Singleton
class OrganizationMutationsController(
    private val resource: OrganizationsResource,
    private val organizations: OrganizationFactory
) : OrganizationMutations {

    private fun OrganizationInput.toRepresentation() = OrganizationRepresentation()
        .name(this.name)
        .displayName(this.displayName)

    override suspend fun organization(input: OrganizationInput): Organization {
        val userId = getAuthentication().userId
        val organization = organizations.findByInput(input)
        val organizationId = if (organization == null) {
            // perform creation
            val rawOrganizationId = resource.create(input.toRepresentation())
            val organizationResource = resource.organization(rawOrganizationId)
            organizationResource.memberships().add(userId.toString())
            val rolesResource = organizationResource.roles()
            Roles.entries.filter { it.isOrganizationRole() }.forEach {
                rolesResource.grant(it.humanName(), userId.toString())
            }

            val organizationId = UUID.fromString(rawOrganizationId)
            organizations.changeCreatedBy(organizationId, userId)
            organizationId
        } else {
            // perform update
            organizations.checkPermission(organization, Permission.MANAGE)
            resource.organization(organization.id.toString()).update(input.toRepresentation())
            organization.id
        }

        return organizations.getById(organizationId).convert()
    }

    override suspend fun deleteOrganization(organizationId: UUID) {
        val organization = organizations.getById(organizationId)
        organizations.checkPermission(organization, Permission.PURGE)
        resource.organization(organization.id.toString()).delete()
        // no event should be produced because tuple was deleted at previous stage
        organizations.deleteById(organization.id)
    }
}
