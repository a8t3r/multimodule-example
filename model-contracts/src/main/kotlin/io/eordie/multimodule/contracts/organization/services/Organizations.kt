package io.eordie.multimodule.contracts.organization.services

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.OrganizationSummary
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.contracts.organization.models.User
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeFilter
import java.util.*

@AutoService(Query::class)
interface OrganizationQueries : Query {

    suspend fun organization(id: UUID): Organization?

    suspend fun organizations(filter: OrganizationsFilter? = null, pageable: Pageable? = null): Page<Organization>

    suspend fun organizationSummary(filter: OrganizationsFilter): OrganizationSummary

    suspend fun loadOrganizationEmployedUsers(
        organizationIds: List<UUID>,
        filter: OrganizationEmployeeFilter? = null
    ): Map<UUID, List<User>>
}

@AutoService(Mutation::class)
interface OrganizationMutations : Mutation
