package io.eordie.multimodule.regions.controllers

import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.regions.models.Region
import io.eordie.multimodule.contracts.regions.service.GeoCodingQueries
import io.eordie.multimodule.regions.repository.RegionsFactory
import jakarta.inject.Singleton

@Singleton
class GeoCodingQueriesController(
    private val regions: RegionsFactory
) : GeoCodingQueries {
    override suspend fun findRegionsByPoint(point: TPoint, country: String?): List<Region> {
        return regions.findRegionsByPoint(point)
            .filter { country == null || it.country == country }
            .sortedBy { it.depth }
            .map { it.convert() }
    }
}
