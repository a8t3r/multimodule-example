package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.OrganizationDigest
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.OrganizationFilterSummary
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.contracts.organization.services.OrganizationQueries
import io.eordie.multimodule.contracts.utils.orDefault
import io.eordie.multimodule.organization.management.repository.OrganizationFactory
import jakarta.inject.Singleton
import java.util.*

@Singleton
class OrganizationQueriesController(
    private val organizations: OrganizationFactory
) : OrganizationQueries {

    override suspend fun organization(id: UUID): Organization? {
        return organizations.queryById(id)
    }

    override suspend fun organizations(filter: OrganizationsFilter?, pageable: Pageable?): Page<Organization> {
        return organizations.query(filter.orDefault(), pageable)
    }

    override suspend fun organizationSummary(filter: OrganizationsFilter?): OrganizationFilterSummary {
        return organizations.getOrganizationsFilterSummary(filter.orDefault())
    }

    override suspend fun loadOrganizationDigest(organizationIds: List<UUID>): Map<UUID, OrganizationDigest> {
        val filter = OrganizationsFilter(id = UUIDLiteralFilter(of = organizationIds))
        return organizations.getOrganizationsDigest(filter)
            .associateBy(OrganizationDigest::organizationId) { it }
    }
}
