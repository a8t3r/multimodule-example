package io.eordie.multimodule.contracts.organization.services

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.models.User
import io.eordie.multimodule.contracts.organization.models.UsersFilter
import io.eordie.multimodule.contracts.utils.Roles
import java.util.*

@AutoService(Query::class)
interface UserQueries : Query {

    suspend fun users(filter: UsersFilter? = null, pageable: Pageable? = null): Page<User>

    suspend fun loadUserByIds(ids: List<UUID>): Map<UUID, User>

    suspend fun loadRolesByUserIds(userIds: List<UUID>, role: Roles?): Map<UUID, List<Roles>>
}

@AutoService(Mutation::class)
interface UserMutations : Mutation
