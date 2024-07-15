package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.filter.acceptMany
import io.eordie.multimodule.common.repository.ResourceAcl
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.basic.Permission.MANAGE
import io.eordie.multimodule.contracts.basic.Permission.PURGE
import io.eordie.multimodule.contracts.basic.Permission.VIEW
import io.eordie.multimodule.contracts.organization.models.acl.FarmAcl
import io.eordie.multimodule.contracts.organization.models.acl.FarmAclFilter
import io.eordie.multimodule.organization.management.models.acl.FarmAclModel
import io.eordie.multimodule.organization.management.models.acl.farmId
import io.eordie.multimodule.organization.management.models.acl.farmOwnerOrganizationId
import io.eordie.multimodule.organization.management.models.acl.fieldIds
import io.eordie.multimodule.organization.management.models.acl.organizationId
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class FarmAclFactory : BaseOrganizationFactory<FarmAclModel, FarmAcl, UUID, FarmAclFilter>(
    FarmAclModel::class
) {
    override val organizationId = FarmAclModel::organizationId

    override suspend fun calculatePermissions(acl: ResourceAcl, value: FarmAclModel): Set<Permission> {
        val currentOrganizationId = acl.auth.currentOrganizationId
        return when {
            value.farmOwnerOrganizationId == currentOrganizationId -> setOf(VIEW, MANAGE, PURGE)
            value.organizationId == currentOrganizationId -> setOf(VIEW)
            else -> emptySet()
        }
    }

    override fun listPredicates(acl: ResourceAcl, table: KNonNullTable<FarmAclModel>) = listOfNotNull(
        or(
            table.organizationId eq acl.auth.currentOrganizationId,
            table.farmOwnerOrganizationId eq acl.auth.currentOrganizationId
        )
    )

    override fun toPredicates(
        acl: ResourceAcl,
        filter: FarmAclFilter,
        table: KNonNullTable<FarmAclModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.farmId.accept(filter.farmId),
            table.fieldIds.acceptMany(filter.fieldId),
            filter.organization?.let {
                registry.toPredicates(acl, it, table.asTableEx())
            },
            filter.farmOwnerOrganization?.let {
                registry.toPredicates(acl, it, table.asTableEx())
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