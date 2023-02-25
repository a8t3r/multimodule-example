package io.eordie.multimodule.organization.controllers

import io.eordie.multimodule.example.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.example.contracts.basic.paging.Page
import io.eordie.multimodule.example.contracts.basic.paging.Pageable
import io.eordie.multimodule.example.contracts.organization.models.Organization
import io.eordie.multimodule.example.contracts.organization.models.OrganizationSummary
import io.eordie.multimodule.example.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.example.contracts.organization.models.User
import io.eordie.multimodule.example.contracts.organization.models.UsersFilter
import io.eordie.multimodule.example.contracts.organization.services.OrganizationQueries
import io.eordie.multimodule.example.utils.associateByList
import io.eordie.multimodule.example.utils.convert
import io.eordie.multimodule.organization.models.UserModel
import io.eordie.multimodule.organization.models.by
import io.eordie.multimodule.organization.repository.OrganizationRepository
import io.eordie.multimodule.organization.repository.UserFactory
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*

@Singleton
class OrganizationQueriesController(
    private val users: UserFactory,
    private val organizations: OrganizationRepository
) : OrganizationQueries {

    override suspend fun organization(id: UUID): Organization? {
        return organizations.findById(id)?.convert()
    }

    override suspend fun organizations(filter: OrganizationsFilter?, pageable: Pageable?): Page<Organization> {
        return organizations.findByFilter(filter ?: OrganizationsFilter(), pageable = pageable ?: Pageable()).convert()
    }

    override suspend fun organizationSummary(filter: OrganizationsFilter): OrganizationSummary {
        return organizations.getOrganizationSummary(filter)
    }

    override suspend fun loadOrganizationMembers(organizationIds: List<UUID>): Map<UUID, List<User>> {
        val filter = UsersFilter(organization = OrganizationsFilter(id = UUIDLiteralFilter(of = organizationIds)))
        val fetcher = newFetcher(UserModel::class).by {
            allScalarFields()
            membership {
                organizationId()
            }
        }

        return users.findByFilter(filter, fetcher, Pageable())
            .associateByList(organizationIds, UserModel::organizationIds)
    }
}
