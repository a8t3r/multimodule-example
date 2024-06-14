package io.eordie.multimodule.contracts.organization.services

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.annotations.Secured
import io.eordie.multimodule.contracts.annotations.Valid
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.organization.models.acl.EmployeeAcl
import io.eordie.multimodule.contracts.organization.models.acl.FarmAcl
import io.eordie.multimodule.contracts.organization.models.acl.FarmAclFilter
import io.eordie.multimodule.contracts.organization.models.acl.FarmAclInput
import java.util.*
import kotlin.coroutines.CoroutineContext

@AutoService(Query::class)
interface FarmAclQueries : Query {
    suspend fun queryFarmAcl(filter: FarmAclFilter? = null, pageable: Pageable? = null): Page<FarmAcl>
}

@AutoService(Mutation::class)
interface FarmAclMutations : Mutation {
    suspend fun farmAcl(
        @GraphQLIgnore
        currentOrganization: CurrentOrganization,
        @Valid farmAcl: FarmAclInput
    ): FarmAcl

    suspend fun deleteFarmAcl(farmAclId: UUID): Boolean
}

@AutoService(Query::class)
interface EmployeeAclQueries : Query {
    suspend fun currentEmployeeAcl(): List<EmployeeAcl>

    @Secured(allowAnonymous = true)
    fun loadEmployeeAcl(context: CoroutineContext, userId: UUID, organizationId: UUID): List<EmployeeAcl>
}
