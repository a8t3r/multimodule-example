package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.organization.models.acl.FarmAcl
import io.eordie.multimodule.contracts.organization.models.acl.FarmAclFilter
import io.eordie.multimodule.contracts.organization.models.acl.FarmAclInput
import io.eordie.multimodule.contracts.organization.services.FarmAclMutations
import io.eordie.multimodule.contracts.organization.services.FarmAclQueries
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.contracts.utils.orDefault
import io.eordie.multimodule.organization.management.repository.FarmAclFactory
import jakarta.inject.Singleton
import java.util.*

@Singleton
class FarmAclController(private val factory: FarmAclFactory) : FarmAclMutations, FarmAclQueries {

    override suspend fun queryFarmAcl(filter: FarmAclFilter?, pageable: Pageable?): Page<FarmAcl> {
        return factory.query(filter.orDefault(), pageable)
    }

    override suspend fun farmAcl(farmAclId: UUID): FarmAcl? {
        return factory.queryById(farmAclId)
    }

    private fun getRegionIds(): List<Long> {
        return emptyList()
    }

    override suspend fun farmAcl(currentOrganization: CurrentOrganization, farmAcl: FarmAclInput): FarmAcl {
        val farmRegionIds = getRegionIds()

        return factory.save(farmAcl.id) { state, value ->
            state.ifNotExists {
                value.farmId = farmAcl.farmId
                value.organizationId = farmAcl.organisationId
                value.farmOwnerOrganizationId = currentOrganization.id
            }

            value.farmRegion {
                farmId = farmAcl.farmId
                regionIds = farmRegionIds
            }

            value.fieldIds = farmAcl.fieldIds
            value.roleIds = Roles.toIds(farmAcl.roles)
        }.convert()
    }

    override suspend fun deleteFarmAcl(farmAclId: UUID): Boolean {
        return factory.deleteById(farmAclId)
    }
}
