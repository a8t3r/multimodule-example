package io.eordie.multimodule.contracts.regions.service

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.regions.models.Region

@AutoService(Query::class)
interface GeoCodingQueries : Query {
    suspend fun findRegionsByPoint(point: TPoint, country: String? = null): List<Region>
}
