package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.ResourceAcl
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPositionFilter
import io.eordie.multimodule.organization.management.models.OrganizationPositionModel
import io.eordie.multimodule.organization.management.models.by
import io.eordie.multimodule.organization.management.models.name
import io.eordie.multimodule.organization.management.models.organization
import io.eordie.multimodule.organization.management.models.organizationId
import io.eordie.multimodule.organization.management.models.parentId
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.toList
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*

@Singleton
class OrganizationPositionFactory :
    BaseOrganizationFactory<OrganizationPositionModel, OrganizationPosition, UUID, OrganizationPositionFilter>(
        OrganizationPositionModel::class
    ) {

    override val organizationId = OrganizationPositionModel::organizationId

    override fun ResourceAcl.toPredicates(
        filter: OrganizationPositionFilter,
        table: KNonNullTable<OrganizationPositionModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.name.accept(filter.name),
            table.parentId.accept(filter.parentId),
            table.organization.accept(filter.organization),
            table.organizationId.accept(filter.organizationId)
        )
    }

    suspend fun changeParent(previousParentId: UUID, newParentId: UUID?): Boolean {
        return rawUpdate {
            set(table.parentId, newParentId)
            where(table.parentId eq previousParentId)
        }
    }

    suspend fun deletePosition(positionId: UUID): Boolean {
        val position = findById(positionId) ?: return false
        changeParent(position.id, position.parentId)

        return deleteById(positionId)
    }

    suspend fun findByIdsWithSubordinates(ids: List<UUID>): List<OrganizationPositionModel> {
        return findByIds(
            ids,
            newFetcher(OrganizationPositionModel::class).by {
                allScalarFields()
                subordinates {
                    allScalarFields()
                }
            }
        ).toList()
    }
}
