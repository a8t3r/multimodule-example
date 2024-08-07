package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.KBaseFactory
import io.eordie.multimodule.common.repository.entity.PermissionAwareIF
import io.eordie.multimodule.contracts.basic.BasePermission
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.basic.filters.Direction
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.contracts.organization.models.invitation.Invitation
import io.eordie.multimodule.contracts.organization.models.invitation.InvitationFilter
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.InvitationModel
import io.eordie.multimodule.organization.management.models.email
import io.eordie.multimodule.organization.management.models.organizationId
import io.eordie.multimodule.organization.management.models.status
import io.eordie.multimodule.organization.management.models.userId
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class InvitationFactory : KBaseFactory<InvitationModel, Invitation, UUID, InvitationFilter>(
    InvitationModel::class
) {
    override val datasourceName = "keycloak"

    override suspend fun calculatePermissions(acl: ResourceAcl, value: InvitationModel): Set<Permission> {
        return when {
            acl.hasOrganizationRole(Roles.MANAGE_INVITATIONS) -> PermissionAwareIF.ALL
            acl.hasOrganizationRole(Roles.VIEW_INVITATIONS) -> setOf(BasePermission.VIEW)
            value.email == acl.auth.email -> setOf(BasePermission.VIEW)
            value.userId == acl.auth.userId -> setOf(BasePermission.VIEW)
            else -> emptySet()
        }
    }

    override fun listPredicates(
        acl: ResourceAcl,
        table: KNonNullTable<InvitationModel>
    ): List<KNonNullExpression<Boolean>> = listOfNotNull(
        or(
            table.email eq acl.auth.email,
            table.userId eq acl.auth.userId,
            (table.organizationId eq acl.auth.currentOrganizationId)
                .takeIf { acl.hasAllOrganizationRoles(Roles.VIEW_INVITATIONS) }
        )
    )

    override fun ResourceAcl.toPredicates(
        filter: InvitationFilter,
        table: KNonNullTable<InvitationModel>
    ): List<KNonNullExpression<Boolean>> = listOfNotNull(
        table.email.accept(filter.email),
        table.organizationId.accept(filter.organizationId),
        table.status.accept(filter.status),
        filter.direction?.let {
            if (it == Direction.INCOME) {
                table.email eq auth.email
            } else {
                table.organizationId eq auth.currentOrganizationId
            }
        }
    )
}
