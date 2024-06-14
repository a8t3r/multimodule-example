package io.eordie.multimodule.regions.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.KBaseFactory
import io.eordie.multimodule.common.repository.ResourceAcl
import io.eordie.multimodule.common.repository.ext.contains
import io.eordie.multimodule.common.repository.ext.jsonStr
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.regions.models.Region
import io.eordie.multimodule.contracts.regions.models.RegionsFilter
import io.eordie.multimodule.regions.models.OsmPlace
import io.eordie.multimodule.regions.models.OsmRegionTreeModel
import io.eordie.multimodule.regions.models.OsmRelationModel
import io.eordie.multimodule.regions.models.country
import io.eordie.multimodule.regions.models.depth
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
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable

@Singleton
class RegionsFactory : KBaseFactory<OsmRegionTreeModel, Region, Long, RegionsFilter>(OsmRegionTreeModel::class) {

    override val datasourceName = "osm"
    override val requireEmployeeAcl = false

    override fun sortingExpressions(table: KNonNullTable<OsmRegionTreeModel>): List<KPropExpression<out Comparable<*>>> {
        return listOf(table.country, table.depth, table.id)
    }

    private fun KNonNullPropExpression<Map<String, String>>.name(lang: String? = null): KNullableExpression<String> {
        val tag = if (lang == null) "name" else "name:${lang.lowercase()}"
        return this.jsonStr(tag)
    }

    override fun toPredicates(
        acl: ResourceAcl,
        filter: RegionsFilter,
        table: KNonNullTable<OsmRegionTreeModel>
    ): List<KNonNullExpression<Boolean>> = listOfNotNull(
        table.country.accept(filter.country),
        table.depth.accept(filter.depth),
        table.parentId.accept(filter.parentId),
        filter.name?.let {
            val ex = table.relation.asTableEx()
            or(
                ex.tags.name().accept(it),
                ex.tags.name(acl.auth.locale.language).accept(it)
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
