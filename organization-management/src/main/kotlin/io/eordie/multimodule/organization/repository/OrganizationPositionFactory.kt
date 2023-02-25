package io.eordie.multimodule.organization.repository

import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPositionFilter
import io.eordie.multimodule.example.filter.accept
import io.eordie.multimodule.example.repository.KBaseFactory
import io.eordie.multimodule.organization.models.OrganizationPositionModel
import io.eordie.multimodule.organization.models.OrganizationPositionModelDraft
import io.eordie.multimodule.organization.models.OrganizationPositionModelFetcherDsl
import io.eordie.multimodule.organization.models.by
import io.eordie.multimodule.organization.models.name
import io.eordie.multimodule.organization.models.organizationId
import io.eordie.multimodule.organization.models.parentId
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.toList
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*
import kotlin.coroutines.CoroutineContext

@Singleton
class OrganizationPositionFactory : KBaseFactory<OrganizationPositionModel, UUID, OrganizationPositionFilter>(
    OrganizationPositionModel::class,
    OrganizationPositionModelDraft.`$`.type
) {

    override val datasourceName = "keycloak"

    override fun toPredicates(
        context: CoroutineContext,
        filter: OrganizationPositionFilter,
        table: KNonNullTable<OrganizationPositionModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.name.accept(filter.name),
            table.parentId.accept(filter.parentId),
            table.organizationId.accept(filter.organizationId)
        )
    }

    private val positionProjection: OrganizationPositionModelFetcherDsl.() -> Unit = {
        allScalarFields()
        parentId()
        deleted()
        roles {
            allScalarFields()
        }
    }

    override val defaultFetcher = newFetcher(entityType).by(positionProjection)

    fun changeParent(previousParentId: UUID, newParentId: UUID?): Boolean {
        return sql.createUpdate(entityType) {
            set(table.parentId, newParentId)
            where(table.parentId eq previousParentId)
        }.execute() > 0
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
                positionProjection(this)
                subordinates {
                    positionProjection(this)
                }
            }
        ).toList()
    }
}
