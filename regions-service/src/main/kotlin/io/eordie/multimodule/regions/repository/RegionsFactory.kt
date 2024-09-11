package io.eordie.multimodule.regions.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.KBaseFactory
import io.eordie.multimodule.common.repository.ext.contains
import io.eordie.multimodule.common.repository.ext.jsonStr
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.contracts.regions.models.Region
import io.eordie.multimodule.contracts.regions.models.RegionsFilter
import io.eordie.multimodule.regions.models.OsmPlace
import io.eordie.multimodule.regions.models.OsmRegionTreeModel
import io.eordie.multimodule.regions.models.OsmRegionTreeModelDraft
import io.eordie.multimodule.regions.models.OsmRelationModel
import io.eordie.multimodule.regions.models.country
import io.eordie.multimodule.regions.models.depth
import io.eordie.multimodule.regions.models.fetchBy
import io.eordie.multimodule.regions.models.geometry
import io.eordie.multimodule.regions.models.id
import io.eordie.multimodule.regions.models.osmId
import io.eordie.multimodule.regions.models.osmType
import io.eordie.multimodule.regions.models.parentId
import io.eordie.multimodule.regions.models.relation
import io.eordie.multimodule.regions.models.tags
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.toList
import org.babyfish.jimmer.sql.ast.Selection
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNullableExpression
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable

@Singleton
class RegionsFactory : KBaseFactory<OsmRegionTreeModel, OsmRegionTreeModelDraft, Region, Long, RegionsFilter>(
    OsmRegionTreeModel::class
) {

    override val datasourceName = "osm"
    override val requireEmployeeAcl = false

    private fun KNonNullPropExpression<Map<String, String>>.name(lang: String? = null): KNullableExpression<String> {
        val tag = if (lang == null) "name" else "name:${lang.lowercase()}"
        return this.jsonStr(tag)
    }

    fun getRegionPath(regionId: Long): List<OsmRegionTreeModel> {
        val region = sql.createQuery(entityType) {
            where(table.id eq regionId)
            select(
                table.fetchBy {
                    allScalarFields()
                    `parent*`()
                }
            )
        }.fetchOneOrNull() ?: return emptyList()

        fun visitParent(region: OsmRegionTreeModel?, visited: MutableList<OsmRegionTreeModel>) {
            if (region != null) {
                visited.add(region)
                visitParent(region.parent, visited)
            }
        }

        return mutableListOf<OsmRegionTreeModel>()
            .apply { visitParent(region, this) }
            .sortedBy { it.depth }
    }

    override fun ResourceAcl.toPredicates(
        filter: RegionsFilter,
        table: KNonNullTable<OsmRegionTreeModel>
    ): List<KNonNullExpression<Boolean>> = listOfNotNull(
        table.id.accept(filter.id),
        table.country.accept(filter.country),
        table.depth.accept(filter.depth),
        table.parentId.accept(filter.parentId),
        filter.name?.let {
            val ex = table.relation.asTableEx()
            or(
                ex.tags.name().accept(it),
                ex.tags.name(auth.locale.language).accept(it)
            )
        }
    )

    fun <T> queryPlaceBySelection(regionIds: List<Long>, selection: (KNonNullTable<OsmPlace>) -> Selection<T>) =
        sql.createQuery(OsmPlace::class) {
            where(
                table.id.osmType eq 'R',
                table.id.osmId valueIn regionIds
            )
            select(table.id.osmId, selection(table))
        }.execute().associateBy({ it._1 }, { it._2 })

    suspend fun findRegionsByPoint(point: TPoint): List<OsmRegionTreeModel> {
        val ids = sql.createQuery(OsmPlace::class) {
            where(
                table.id.osmType eq 'R',
                table.geometry.contains(point)
            )
            select(table.id.osmId)
        }.execute()

        return findByIds(ids).toList()
    }

    fun getRegionsName(regionIds: List<Long>, lang: String): Map<Long, String> {
        return sql.createQuery(OsmRelationModel::class) {
            where(table.id valueIn regionIds)
            select(
                table.id,
                table.tags.name(),
                table.tags.name(lang)
            )
        }.execute().associateBy({ it._1 }, { it._2 ?: it._3 ?: "<unspecified>" })
    }
}
