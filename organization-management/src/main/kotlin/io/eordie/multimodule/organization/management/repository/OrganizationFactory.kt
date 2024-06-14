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

    override fun toPredicates(
        acl: ResourceAcl,
        filter: OrganizationsFilter,
        table: KNonNullTable<OrganizationModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.id.accept(filter.id),
            table.name.accept(filter.name),
            filter.domains?.let {
                table.domains {
                    registry.toPredicates(acl, filter, asTableEx())
                }
            },
            filter.members
                ?.takeIf { acl.hasAnyOrganizationRole(Roles.VIEW_MEMBERS, Roles.MANAGE_ORGANIZATIONS) }
                ?.let {
                    table.members {
                        registry.toPredicates(acl, it, asTableEx())
                    }
                }
        )
    }
}
