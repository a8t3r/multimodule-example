package io.eordie.multimodule.contracts.organization.services

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.OrganizationDigest
import io.eordie.multimodule.contracts.organization.OrganizationInput
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.OrganizationFilterSummary
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import java.util.*

@AutoService(Query::class)
interface OrganizationQueries : Query {

    suspend fun organization(id: UUID): Organization?

    suspend fun organizations(filter: OrganizationsFilter? = null, pageable: Pageable? = null): Page<Organization>

    suspend fun organizationSummary(filter: OrganizationsFilter?): OrganizationFilterSummary

    suspend fun loadOrganizationDigest(organizationIds: List<UUID>): Map<UUID, OrganizationDigest>
}

@AutoService(Mutation::class)
interface OrganizationMutations : Mutation {
    suspend fun organization(input: OrganizationInput): Organization

    suspend fun organizationByVat(vat: String): Organization

    suspend fun deleteOrganization(organizationId: UUID)
}
