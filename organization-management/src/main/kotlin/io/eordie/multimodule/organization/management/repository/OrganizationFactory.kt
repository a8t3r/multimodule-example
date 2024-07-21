package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.ResourceAcl
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.OrganizationModel
import io.eordie.multimodule.organization.management.models.displayName
import io.eordie.multimodule.organization.management.models.domains
import io.eordie.multimodule.organization.management.models.id
import io.eordie.multimodule.organization.management.models.members
import io.eordie.multimodule.organization.management.models.name
import io.eordie.multimodule.organization.management.models.user
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class OrganizationFactory : BaseOrganizationFactory<OrganizationModel, Organization, UUID, OrganizationsFilter>(
    OrganizationModel::class
) {

    override val organizationId = OrganizationModel::id

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
            table.domains { accept(filter.domains) },
            table.members { user.accept(filter.members?.takeIf { applyMembershipFilter }) }
        )
    }
}
