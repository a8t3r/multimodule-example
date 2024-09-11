package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.entity.PermissionAwareIF
import io.eordie.multimodule.common.repository.ext.arrayAgg
import io.eordie.multimodule.common.repository.ext.asList
import io.eordie.multimodule.common.repository.ext.or
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.organization.OrganizationDigest
import io.eordie.multimodule.contracts.organization.OrganizationInput
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.OrganizationFilterSummary
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.OrganizationModel
import io.eordie.multimodule.organization.management.models.OrganizationModelDraft
import io.eordie.multimodule.organization.management.models.createdBy
import io.eordie.multimodule.organization.management.models.createdByStr
import io.eordie.multimodule.organization.management.models.`departments?`
import io.eordie.multimodule.organization.management.models.domains
import io.eordie.multimodule.organization.management.models.`domains?`
import io.eordie.multimodule.organization.management.models.`employees?`
import io.eordie.multimodule.organization.management.models.id
import io.eordie.multimodule.organization.management.models.`information?`
import io.eordie.multimodule.organization.management.models.inn
import io.eordie.multimodule.organization.management.models.members
import io.eordie.multimodule.organization.management.models.`members?`
import io.eordie.multimodule.organization.management.models.name
import io.eordie.multimodule.organization.management.models.`positions?`
import io.eordie.multimodule.organization.management.models.user
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.count
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.value
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class OrganizationFactory : BaseOrganizationFactory<OrganizationModel, OrganizationModelDraft, Organization, UUID, OrganizationsFilter>(
    OrganizationModel::class
) {

    override val organizationId = OrganizationModel::id

    override val dependencies = setOf(OrganizationModel::createdBy)

    suspend fun getOrganizationsDigest(filter: OrganizationsFilter): List<OrganizationDigest> {
        val resourceAcl = resourceAcl()
        val tuples = sql.createQuery(entityType) {
            where(*resourceAcl.toPredicates(filter, table).toTypedArray())
            val ext = table.asTableEx()
            groupBy(table.id)

            select(
                table.id,
                count(ext.`members?`.id, true),
                count(ext.`domains?`.id, true),
                count(ext.`departments?`.id, true),
                count(ext.`positions?`.id, true),
                count(ext.`employees?`.id, true),
            )
        }.execute()

        return tuples.map { tupleConverter.convert(it, OrganizationDigest::class) }
    }

    suspend fun getOrganizationsFilterSummary(filter: OrganizationsFilter): OrganizationFilterSummary {
        val resourceAcl = resourceAcl()
        val tuple = sql.createQuery(entityType) {
            where(*resourceAcl.toPredicates(filter, table).toTypedArray())
            val ext = table.asTableEx()

            select(
                count(table.id, true),
                table.id.arrayAgg(),
                ext.`domains?`.id.arrayAgg(),
                ext.`members?`.id.arrayAgg()
            )
        }.fetchOne()

        return tupleConverter.convert(tuple, OrganizationFilterSummary::class)
    }

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

    override fun ResourceAcl.listPredicates(
        table: KNonNullTable<OrganizationModel>
    ): List<KNonNullExpression<Boolean>> = listOf(
        table.createdBy eq auth.userId,
        when {
            hasOrganizationRole(Roles.MANAGE_ORGANIZATIONS) -> value(true)
            hasAllOrganizationRoles(viewRoles) -> {
                table.get<UUID>(organizationId.name) valueIn allOrganizationIds
            }

            else -> value(false)
        }
    ).or().asList()

    override suspend fun calculatePermissions(acl: ResourceAcl, value: OrganizationModel): Set<Permission> {
        return if (value.createdBy == acl.auth.userId) PermissionAwareIF.ALL else {
            super.calculatePermissions(acl, value)
        }
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
            table.members { user.accept(filter.members?.takeIf { applyMembershipFilter }) },
            filter.information?.let {
                table.`information?`.inn.accept(it.vat)
            }
        )
    }
}
