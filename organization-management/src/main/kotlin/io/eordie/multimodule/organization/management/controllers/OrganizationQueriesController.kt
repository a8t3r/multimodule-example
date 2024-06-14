package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.utils.associateById
import io.eordie.multimodule.common.utils.convert
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.OrganizationSummary
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.contracts.organization.models.User
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeFilter
import io.eordie.multimodule.contracts.organization.services.OrganizationQueries
import io.eordie.multimodule.contracts.utils.orDefault
import io.eordie.multimodule.organization.management.models.OrganizationEmployeeModel
import io.eordie.multimodule.organization.management.models.by
import io.eordie.multimodule.organization.management.repository.OrganizationEmployeeFactory
import io.eordie.multimodule.organization.management.repository.OrganizationRepository
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*

@Singleton
class OrganizationQueriesController(
    private val organizations: OrganizationRepository,
    private val employees: OrganizationEmployeeFactory
) : OrganizationQueries {

    override suspend fun organization(id: UUID): Organization? {
        return organizations.findById(id)?.convert()
    }

    override suspend fun organizations(filter: OrganizationsFilter?, pageable: Pageable?): Page<Organization> {
        return organizations.findByFilter(filter.orDefault(), pageable).convert()
    }

    override suspend fun organizationSummary(filter: OrganizationsFilter): OrganizationSummary {
        return organizations.getOrganizationSummary(employees.resourceAcl(), filter)
    }

    override suspend fun loadOrganizationEmployedUsers(
        organizationIds: List<UUID>,
        filter: OrganizationEmployeeFilter?
    ): Map<UUID, List<User>> {
        val filterBy = filter.orDefault()
            .copy(organizationId = UUIDLiteralFilter(of = organizationIds))

        val fetcher = newFetcher(OrganizationEmployeeModel::class).by {
            allScalarFields()
            organizationId()
            user {
                allScalarFields()
            }
        }

        return employees.findAllByFilter(filterBy, fetcher = fetcher)
            .associateById(organizationIds, { it.organizationId }, { it.user.convert() })
    }
}
