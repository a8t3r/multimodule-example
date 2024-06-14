package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.ResourceAcl
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployee
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeFilter
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.OrganizationEmployeeModel
import io.eordie.multimodule.organization.management.models.department
import io.eordie.multimodule.organization.management.models.member
import io.eordie.multimodule.organization.management.models.organization
import io.eordie.multimodule.organization.management.models.organizationId
import io.eordie.multimodule.organization.management.models.position
import io.eordie.multimodule.organization.management.models.user
import io.eordie.multimodule.organization.management.models.userId
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.eq
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

    override fun toPredicates(
        acl: ResourceAcl,
        filter: OrganizationEmployeeFilter,
        table: KNonNullTable<OrganizationEmployeeModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            // membership is required for employment
            table.member.organizationId eq table.organizationId,
            table.organizationId.accept(filter.organizationId),
            table.userId.accept(filter.userId),
            filter.user?.let {
                registry.toPredicates(acl, it, table.user.asTableEx())
            },
            filter.organization?.let {
                registry.toPredicates(acl, it, table.organization.asTableEx())
            },
            filter.department?.let {
                registry.toPredicates(acl, it, table.department.asTableEx())
            },
            filter.position?.let {
                registry.toPredicates(acl, it, table.position.asTableEx())
            }
        )
    }
}
