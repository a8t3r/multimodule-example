package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.ext.arrayAgg
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployee
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeFilter
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeFilterSummary
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.OrganizationEmployeeModel
import io.eordie.multimodule.organization.management.models.department
import io.eordie.multimodule.organization.management.models.departmentId
import io.eordie.multimodule.organization.management.models.fetchBy
import io.eordie.multimodule.organization.management.models.id
import io.eordie.multimodule.organization.management.models.member
import io.eordie.multimodule.organization.management.models.organization
import io.eordie.multimodule.organization.management.models.organizationId
import io.eordie.multimodule.organization.management.models.position
import io.eordie.multimodule.organization.management.models.positionId
import io.eordie.multimodule.organization.management.models.user
import io.eordie.multimodule.organization.management.models.userId
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.count
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class OrganizationEmployeeFactory :
    BaseOrganizationFactory<OrganizationEmployeeModel, OrganizationEmployee, UUID, OrganizationEmployeeFilter>(
        OrganizationEmployeeModel::class
    ) {

    override val organizationId = OrganizationEmployeeModel::organizationId
    override val viewRoles = setOf(Roles.VIEW_ORGANIZATION, Roles.VIEW_MEMBERS)
    override val manageRoles = setOf(Roles.MANAGE_ORGANIZATION, Roles.MANAGE_MEMBERS)

    suspend fun getEmployeeFilterSummary(filter: OrganizationEmployeeFilter): OrganizationEmployeeFilterSummary {
        val resourceAcl = resourceAcl()
        val tuple = sql.createQuery(entityType) {
            where(*resourceAcl.toPredicates(filter, table).toTypedArray())
            select(
                count(table.id, true),
                table.userId.arrayAgg(),
                table.organizationId.arrayAgg(),
                table.id.arrayAgg(),
                table.departmentId.arrayAgg(),
                table.positionId.arrayAgg()
            )
        }.fetchOne()

        return tupleConverter.convert(tuple, OrganizationEmployeeFilterSummary::class)
    }

    fun getEmployeesByOrganization(userIds: List<UUID>, organizationId: UUID): List<OrganizationEmployeeModel> {
        return sql.createQuery(entityType) {
            where(
                table.userId valueIn userIds,
                table.organizationId eq organizationId
            )
            select(
                table.fetchBy {
                    allTableFields()

                    organization {
                        name()
                    }

                    user {
                        email()
                        emailVerified()
                    }

                    position {
                        roles()
                    }
                }
            )
        }.execute()
    }

    override fun ResourceAcl.toPredicates(
        filter: OrganizationEmployeeFilter,
        table: KNonNullTable<OrganizationEmployeeModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            // membership is required for employment
            table.member.organizationId eq table.organizationId,
            table.organizationId.accept(filter.organizationId),
            table.userId.accept(filter.userId),
            table.user.accept(filter.user),
            table.organization.accept(filter.organization),
            table.department.accept(filter.department),
            table.position.accept(filter.position)
        )
    }
}
