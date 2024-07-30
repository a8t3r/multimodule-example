package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.rsocket.context.getAuthentication
import io.eordie.multimodule.common.utils.associateBy
import io.eordie.multimodule.common.utils.associateFlattenById
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.models.User
import io.eordie.multimodule.contracts.organization.models.UsersFilter
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeFilter
import io.eordie.multimodule.contracts.organization.services.UserQueries
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.contracts.utils.orDefault
import io.eordie.multimodule.organization.management.models.OrganizationEmployeeModel
import io.eordie.multimodule.organization.management.models.UserModel
import io.eordie.multimodule.organization.management.models.by
import io.eordie.multimodule.organization.management.repository.OrganizationEmployeeFactory
import io.eordie.multimodule.organization.management.repository.UserFactory
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*

@Singleton
class UserQueriesController(
    private val users: UserFactory,
    private val employees: OrganizationEmployeeFactory
) : UserQueries {

    override suspend fun users(filter: UsersFilter?, pageable: Pageable?): Page<User> {
        return users.query(filter.orDefault(), pageable)
    }

    override suspend fun loadUserByIds(ids: List<UUID>): Map<UUID, User> {
        return users.findByIds(ids).associateBy(UserModel::id) { it.convert() }
    }

    override suspend fun loadRolesByUserIds(userIds: List<UUID>, role: String?): Map<UUID, List<String>> {
        val filterBy = OrganizationEmployeeFilter(
            userId = UUIDLiteralFilter(of = userIds),
            organizationId = UUIDLiteralFilter(eq = getAuthentication().currentOrganizationId)
        )

        val fetcher = newFetcher(OrganizationEmployeeModel::class).by {
            allScalarFields()
            userId()
            position {
                allScalarFields()
            }
        }
        return employees.findAllByFilter(filterBy, fetcher)
            .associateFlattenById(userIds, OrganizationEmployeeModel::userId) { employee ->
                val position = requireNotNull(employee.position)
                Roles.nameFromIds(position.roleIds).filter { role == null || role == it }
            }
    }
}
