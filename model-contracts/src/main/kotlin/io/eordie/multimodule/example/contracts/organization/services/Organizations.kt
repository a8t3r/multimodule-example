package io.eordie.multimodule.example.contracts.organization.services

import com.google.auto.service.AutoService
import io.eordie.multimodule.example.contracts.Mutation
import io.eordie.multimodule.example.contracts.Query
import io.eordie.multimodule.example.contracts.annotations.Secured
import io.eordie.multimodule.example.contracts.basic.paging.Page
import io.eordie.multimodule.example.contracts.basic.paging.Pageable
import io.eordie.multimodule.example.contracts.organization.models.Organization
import io.eordie.multimodule.example.contracts.organization.models.OrganizationSummary
import io.eordie.multimodule.example.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.example.contracts.organization.models.User
import io.eordie.multimodule.example.contracts.utils.Roles
import java.util.*

@AutoService(Query::class)
interface OrganizationQueries : Query {

    suspend fun organization(id: UUID): Organization?

    suspend fun organizations(filter: OrganizationsFilter? = null, pageable: Pageable? = null): Page<Organization>

    suspend fun organizationSummary(filter: OrganizationsFilter): OrganizationSummary

    @Secured(value = [ Roles.VIEW_MEMBERS ])
    suspend fun loadOrganizationMembers(organizationIds: List<UUID>): Map<UUID, List<User>>
}

@AutoService(Mutation::class)
interface OrganizationMutations : Mutation
