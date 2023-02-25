package io.eordie.multimodule.organization.repository

import io.eordie.multimodule.example.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.example.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.example.contracts.utils.Roles
import io.eordie.multimodule.example.filter.accept
import io.eordie.multimodule.example.repository.KBaseFactory
import io.eordie.multimodule.example.repository.entity.Permission
import io.eordie.multimodule.example.utils.ifHasAnyRole
import io.eordie.multimodule.organization.models.OrganizationModel
import io.eordie.multimodule.organization.models.OrganizationModelDraft
import io.eordie.multimodule.organization.models.by
import io.eordie.multimodule.organization.models.displayName
import io.eordie.multimodule.organization.models.domains
import io.eordie.multimodule.organization.models.id
import io.eordie.multimodule.organization.models.members
import io.eordie.multimodule.organization.models.name
import io.eordie.multimodule.organization.utils.isOrganizationMember
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*
import kotlin.coroutines.CoroutineContext

@Singleton
class OrganizationFactory : KBaseFactory<OrganizationModel, UUID, OrganizationsFilter>(
    OrganizationModel::class,
    OrganizationModelDraft.`$`.type
) {

    override val datasourceName = "keycloak"

    override fun sortingExpressions(table: KNonNullTable<OrganizationModel>): List<KPropExpression<out Comparable<*>>> {
        return listOf(
            table.name,
            table.displayName,
            table.id
        )
    }

    override fun calculatePermissions(
        auth: AuthenticationDetails,
        value: OrganizationModel
    ): Set<Permission> {
        return if (auth.hasRole(Roles.MANAGE_ORGANIZATIONS)) {
            setOf(Permission.VIEW, Permission.MANAGE)
        } else {
            val managedOrganizations = auth.organizationsWithRole(Roles.MANAGE_ORGANIZATION)
            buildSet {
                val organizationId = value.id
                if (organizationId in auth.organizationIds) {
                    add(Permission.VIEW)
                    if (organizationId in managedOrganizations) {
                        add(Permission.MANAGE)
                    }
                }
            }
        }
    }

    override fun listPredicates(
        context: CoroutineContext,
        table: KNonNullTable<OrganizationModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.id.isOrganizationMember(context)
        )
    }

    override fun toPredicates(
        context: CoroutineContext,
        filter: OrganizationsFilter,
        table: KNonNullTable<OrganizationModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.id.accept(filter.id),
            table.name.accept(filter.name),
            filter.domains?.let {
                table.domains {
                    registry.toPredicates(context, filter, asTableEx())
                }
            },
            filter.members?.let {
                context.ifHasAnyRole(Roles.VIEW_MEMBERS, Roles.MANAGE_ORGANIZATIONS) {
                    table.members {
                        registry.toPredicates(context, it, asTableEx())
                    }
                }
            }
        )
    }

    suspend fun getOrganizationWithRoles(id: UUID): OrganizationModel {
        return getById(
            id,
            newFetcher(OrganizationModel::class).by {
                allScalarFields()
                roles {
                    allScalarFields()
                }
            }
        )
    }
}
