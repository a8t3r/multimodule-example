package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.utils.convert
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.models.acl.FarmAcl
import io.eordie.multimodule.contracts.organization.models.acl.FarmAclFilter
import io.eordie.multimodule.contracts.organization.services.FarmAclQueries
import io.eordie.multimodule.contracts.utils.orDefault
import io.eordie.multimodule.organization.management.repository.FarmAclFactory
import jakarta.inject.Singleton

@Singleton
class FarmAclQueriesController(private val factory: FarmAclFactory) : FarmAclQueries {
    override suspend fun queryFarmAcl(filter: FarmAclFilter?, pageable: Pageable?): Page<FarmAcl> {
        return factory.findByFilter(filter.orDefault(), pageable).convert()
    }
}
