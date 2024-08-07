package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.entity.PermissionAwareIF
import io.eordie.multimodule.contracts.basic.BasePermission
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequest
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequestFilter
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.AccessionRequestModel
import io.eordie.multimodule.organization.management.models.createdBy
import io.eordie.multimodule.organization.management.models.id
import io.eordie.multimodule.organization.management.models.organizationId
import io.eordie.multimodule.organization.management.models.status
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class AccessionRequestFactory :
    BaseOrganizationFactory<AccessionRequestModel, AccessionRequest, UUID, AccessionRequestFilter>(
        AccessionRequestModel::class
    ) {

    override val organizationId = AccessionRequestModel::organizationId

    override fun listPredicates(acl: ResourceAcl, table: KNonNullTable<AccessionRequestModel>) =
        listOfNotNull(
            or(
                table.createdBy eq acl.auth.userId,
                (table.organizationId valueIn acl.auth.organizationIds()).takeIf {
                    acl.hasAnyOrganizationRole(Roles.VIEW_INVITATIONS)
                }
            )
        )

    override suspend fun calculatePermissions(acl: ResourceAcl, value: AccessionRequestModel) =
        when {
            value.createdBy == acl.auth.userId -> PermissionAwareIF.ALL
            value.organizationId in acl.auth.organizationIds() -> {
                if (acl.hasOrganizationRole(value.organizationId, Roles.MANAGE_INVITATIONS)) {
                    setOf(BasePermission.VIEW, BasePermission.MANAGE)
                } else {
                    setOf(BasePermission.VIEW)
                }
            }
            else -> emptySet()
        }

    override fun ResourceAcl.toPredicates(
        filter: AccessionRequestFilter,
        table: KNonNullTable<AccessionRequestModel>
    ) = listOfNotNull(
        table.id.accept(filter.id),
        table.status.accept(filter.status),
        table.organizationId.accept(filter.organizationId)
    )
}
