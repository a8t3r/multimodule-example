package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.entity.PermissionAwareIF
import io.eordie.multimodule.common.repository.ext.and
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.organization.OrganizationInput
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.OrganizationModel
import io.eordie.multimodule.organization.management.models.createdBy
import io.eordie.multimodule.organization.management.models.createdByStr
import io.eordie.multimodule.organization.management.models.displayName
import io.eordie.multimodule.organization.management.models.domains
import io.eordie.multimodule.organization.management.models.id
import io.eordie.multimodule.organization.management.models.members
import io.eordie.multimodule.organization.management.models.name
import io.eordie.multimodule.organization.management.models.user
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class OrganizationFactory : BaseOrganizationFactory<OrganizationModel, Organization, UUID, OrganizationsFilter>(
    OrganizationModel::class
) {

    override val organizationId = OrganizationModel::id

    override val dependencies = setOf(OrganizationModel::createdBy)

    suspend fun changeCreatedBy(organizationId: UUID, userId: UUID) {
        wrapped {
            sql.createUpdate(OrganizationModel::class) {
                set(table.createdByStr, userId.toString())
                where(table.id eq organizationId)
            }.execute(it)
        }
    }

    fun findByInput(input: OrganizationInput): OrganizationModel? {
        return sql.createQuery(entityType) {
            where(
                if (input.id != null) {
                    table.id eq input.id
                } else {
                    table.name eq input.name
                }
            )
            select(table)
        }.fetchOneOrNull()
    }

    override fun listPredicates(
        acl: ResourceAcl,
        table: KNonNullTable<OrganizationModel>
    ): List<KNonNullExpression<Boolean>> = listOf(
        or(
            table.createdBy eq acl.auth.userId,
            super.listPredicates(acl, table).and()
        )!!
    )

    override suspend fun calculatePermissions(acl: ResourceAcl, value: OrganizationModel): Set<Permission> {
        return if (value.createdBy == acl.auth.userId) PermissionAwareIF.ALL else {
            super.calculatePermissions(acl, value)
        }
    }

    override fun sortingExpressions(table: KNonNullTable<OrganizationModel>): List<KPropExpression<out Comparable<*>>> {
        return listOf(
            table.name,
            table.displayName,
            table.id
        )
    }

    override fun ResourceAcl.toPredicates(
        filter: OrganizationsFilter,
        table: KNonNullTable<OrganizationModel>
    ): List<KNonNullExpression<Boolean>> {
        val applyMembershipFilter = hasAnyOrganizationRole(Roles.VIEW_MEMBERS, Roles.MANAGE_ORGANIZATIONS)
        return listOfNotNull(
            table.id.accept(filter.id),
            table.name.accept(filter.name),
            table.createdBy.accept(filter.createdBy),
            table.domains { accept(filter.domains) },
            table.members { user.accept(filter.members?.takeIf { applyMembershipFilter }) }
        )
    }
}
