package io.eordie.multimodule.regions.controllers

import io.eordie.multimodule.common.rsocket.context.getAuthentication
import io.eordie.multimodule.common.utils.associateById
import io.eordie.multimodule.common.utils.convert
import io.eordie.multimodule.contracts.basic.geometry.TMultiPolygon
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.regions.models.Region
import io.eordie.multimodule.contracts.regions.models.RegionsFilter
import io.eordie.multimodule.contracts.regions.service.RegionQueries
import io.eordie.multimodule.contracts.utils.orDefault
import io.eordie.multimodule.regions.models.centroid
import io.eordie.multimodule.regions.models.geometry
import io.eordie.multimodule.regions.models.parentId
import io.eordie.multimodule.regions.repository.RegionsFactory
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn

@Singleton
class RegionQueriesController(
    private val regions: RegionsFactory
) : RegionQueries {

    override suspend fun region(regionId: Long): Region? {
        return regions.findById(regionId)?.convert()
    }

    override suspend fun regions(filter: RegionsFilter?, pageable: Pageable?): Page<Region> {
        return regions.findByFilter(filter.orDefault(), pageable).convert()
    }

    override suspend fun loadRegionByParentIds(parentIds: List<Long>): Map<Long, List<Region>> {
        return regions.findAllBySpecification {
            where(table.parentId valueIn parentIds)
        }.associateById(parentIds, { requireNotNull(it.parentId) }) { it.convert() }
    }

    override suspend fun loadRegionsName(regionIds: List<Long>, lang: String?): Map<Long, String> {
        val targetLanguage = lang ?: getAuthentication().locale.language
        return regions.getRegionsName(regionIds, targetLanguage)
    }

    override suspend fun loadGeometriesByRegionId(regionIds: List<Long>): Map<Long, TMultiPolygon> {
        return regions.queryPlaceBySelection(regionIds) { it.geometry }
    }

    override suspend fun loadCentroidsByRegionId(regionIds: List<Long>): Map<Long, TPoint> {
        return regions.queryPlaceBySelection(regionIds) { it.centroid }
    }
}
