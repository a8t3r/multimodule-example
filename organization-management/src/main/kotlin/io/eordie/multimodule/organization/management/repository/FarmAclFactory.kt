package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.filter.acceptMany
import io.eordie.multimodule.common.repository.entity.PermissionAwareIF.Companion.ALL
import io.eordie.multimodule.contracts.basic.BasePermission.VIEW
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.organization.models.Direction
import io.eordie.multimodule.contracts.organization.models.acl.FarmAcl
import io.eordie.multimodule.contracts.organization.models.acl.FarmAclFilter
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.acl.FarmAclModel
import io.eordie.multimodule.organization.management.models.acl.FarmAclModelDraft
import io.eordie.multimodule.organization.management.models.acl.farmId
import io.eordie.multimodule.organization.management.models.acl.farmOwnerOrganization
import io.eordie.multimodule.organization.management.models.acl.farmOwnerOrganizationId
import io.eordie.multimodule.organization.management.models.acl.farmRegion
import io.eordie.multimodule.organization.management.models.acl.fieldIds
import io.eordie.multimodule.organization.management.models.acl.organization
import io.eordie.multimodule.organization.management.models.acl.organizationId
import io.eordie.multimodule.organization.management.models.acl.regionIds
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class FarmAclFactory : BaseOrganizationFactory<FarmAclModel, FarmAclModelDraft, FarmAcl, UUID, FarmAclFilter>(
    FarmAclModel::class
) {
    override val organizationId = FarmAclModel::organizationId

    override suspend fun calculatePermissions(acl: ResourceAcl, value: FarmAclModel): Set<Permission> {
        return when {
            acl.hasOrganizationRole(Roles.MANAGE_ORGANIZATIONS) -> ALL
            value.farmOwnerOrganizationId in acl.auth.organizationIds() -> ALL
            value.organizationId in acl.auth.organizationIds() -> setOf(VIEW)
            else -> emptySet()
        }
    }

    override fun ResourceAcl.listPredicates(table: KNonNullTable<FarmAclModel>) = listOfNotNull(
        or(
            table.organizationId valueIn auth.organizationIds(),
            table.farmOwnerOrganizationId valueIn auth.organizationIds()
        ).takeUnless { hasOrganizationRole(Roles.MANAGE_ORGANIZATIONS) }
    )

    override fun ResourceAcl.toPredicates(
        filter: FarmAclFilter,
        table: KNonNullTable<FarmAclModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            filter.direction?.let {
                val field = if (it == Direction.INCOME) table.organizationId else table.farmOwnerOrganizationId
                field valueIn auth.organizationIds()
            },
            table.farmId accept filter.farmId,
            table.fieldIds acceptMany filter.fieldId,
            table.organization accept filter.organization,
            table.farmOwnerOrganization accept filter.farmOwnerOrganization,
            filter.regionId?.let {
                table.farmRegion.regionIds acceptMany it
            },
            filter.relatedOrganizationId?.let {
                or(
                    table.organizationId accept it,
                    table.farmOwnerOrganizationId accept it
                )
            }
        )
    }

    suspend fun findByOrganizationAndFarm(organizationId: UUID, farmId: UUID): FarmAclModel? {
        return findOneBySpecification {
            where(
                table.organizationId eq organizationId,
                table.farmId eq farmId
            )
        }
    }
}
