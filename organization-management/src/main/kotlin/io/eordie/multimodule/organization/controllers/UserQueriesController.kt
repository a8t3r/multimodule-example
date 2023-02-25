package io.eordie.multimodule.organization.controllers

import io.eordie.multimodule.example.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.example.contracts.basic.paging.Page
import io.eordie.multimodule.example.contracts.basic.paging.Pageable
import io.eordie.multimodule.example.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.example.contracts.organization.models.User
import io.eordie.multimodule.example.contracts.organization.models.UsersFilter
import io.eordie.multimodule.example.contracts.organization.services.UserQueries
import io.eordie.multimodule.example.rsocket.context.getAuthentication
import io.eordie.multimodule.example.utils.associateBy
import io.eordie.multimodule.example.utils.convert
import io.eordie.multimodule.organization.models.UserModel
import io.eordie.multimodule.organization.models.by
import io.eordie.multimodule.organization.models.name
import io.eordie.multimodule.organization.models.organizationId
import io.eordie.multimodule.organization.repository.UserFactory
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*

@Singleton
class UserQueriesController(
    private val users: UserFactory,
) : UserQueries {

    override suspend fun users(filter: UsersFilter?, pageable: Pageable?): Page<User> {
        return users.findByFilter(filter ?: UsersFilter(), pageable = pageable ?: Pageable()).convert()
    }

    override suspend fun loadUserByIds(ids: List<UUID>): Map<UUID, User> {
        return users.findByIds(ids).associateBy(UserModel::id) { it.convert() }
    }

    override suspend fun loadRolesByUserIds(userIds: List<UUID>, role: String?): Map<UUID, List<String>> {
        val currentOrganizationId = getAuthentication().currentOrganizationId
        val filter = UsersFilter(
            id = UUIDLiteralFilter(of = userIds),
            organization = OrganizationsFilter(id = UUIDLiteralFilter(eq = currentOrganizationId))
        )

        val fetcher = newFetcher(UserModel::class).by {
            allScalarFields()
            roles({
                filter {
                    where(
                        table.organizationId eq currentOrganizationId,
                        role?.let { table.name eq role }
                    )
                }
            }) {
                name()
                organizationId()
            }
        }
        return users.findByFilter(filter, fetcher).data
            .associateBy({ it.id }, { user -> user.roles.map { it.name } })
    }
}
