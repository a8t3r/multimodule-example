package io.eordie.multimodule.contracts.regions.service

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.basic.geometry.TMultiPolygon
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.regions.models.Region
import io.eordie.multimodule.contracts.regions.models.RegionsFilter

@AutoService(Query::class)
interface RegionQueries : Query {

    suspend fun region(regionId: Long): Region?

    suspend fun regions(filter: RegionsFilter? = null, pageable: Pageable? = null): Page<Region>

    suspend fun regionPath(regionId: Long): List<Region>

    suspend fun loadRegionByParentIds(parentIds: List<Long>): Map<Long, List<Region>>

    suspend fun loadRegionsName(regionIds: List<Long>, lang: String? = null): Map<Long, String>

    suspend fun loadGeometriesByRegionId(regionIds: List<Long>): Map<Long, TMultiPolygon>

    suspend fun loadCentroidsByRegionId(regionIds: List<Long>): Map<Long, TPoint>
}
