package io.eordie.multimodule.example.contracts.organization.services

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.google.auto.service.AutoService
import io.eordie.multimodule.example.contracts.Mutation
import io.eordie.multimodule.example.contracts.Query
import io.eordie.multimodule.example.contracts.basic.paging.Page
import io.eordie.multimodule.example.contracts.basic.paging.Pageable
import io.eordie.multimodule.example.contracts.organization.models.User
import io.eordie.multimodule.example.contracts.organization.models.UsersFilter
import io.eordie.multimodule.example.contracts.utils.UuidStr
import java.util.*

@AutoService(Query::class)
interface UserQueries : Query {

    suspend fun users(filter: UsersFilter? = null, pageable: Pageable? = null): Page<User>

    suspend fun loadUserByIds(ids: List<UUID>): Map<UUID, User>

    suspend fun loadRolesByUserIds(userIds: List<UUID>, role: String?): Map<UUID, List<String>>
}

@AutoService(Mutation::class)
interface UserMutations : Mutation {

    @GraphQLIgnore
    suspend fun switchOrganization(userId: UuidStr, organizationId: UUID): Boolean
}
