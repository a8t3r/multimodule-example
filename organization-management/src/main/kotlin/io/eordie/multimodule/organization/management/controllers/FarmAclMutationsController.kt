package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.organization.models.acl.FarmAcl
import io.eordie.multimodule.contracts.organization.models.acl.FarmAclInput
import io.eordie.multimodule.contracts.organization.services.FarmAclMutations
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.acl.FarmAclModelDraft
import io.eordie.multimodule.organization.management.repository.FarmAclFactory
import jakarta.inject.Singleton
import java.util.*

@Singleton
class FarmAclMutationsController(private val factory: FarmAclFactory) : FarmAclMutations {
    override suspend fun farmAcl(currentOrganization: CurrentOrganization, farmAcl: FarmAclInput): FarmAcl {
        return factory.save<FarmAclModelDraft>(farmAcl.id) { state, value ->
            state.ifNotExist {
                value.farmId = farmAcl.farmId
                value.organizationId = farmAcl.organisationId
                value.farmOwnerOrganizationId = currentOrganization.id
            }

            value.fieldIds = farmAcl.fieldIds
            value.roleIds = Roles.idsFromNames(farmAcl.roles)
        }.convert()
    }

    override suspend fun deleteFarmAcl(farmAclId: UUID): Boolean {
        return factory.deleteById(farmAclId)
    }
}
