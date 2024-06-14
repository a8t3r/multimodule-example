package io.eordie.multimodule.contracts.regions.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.annotations.Cached
import io.eordie.multimodule.contracts.basic.geometry.TMultiPolygon
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.regions.service.RegionQueries
import io.eordie.multimodule.contracts.utils.byId
import io.eordie.multimodule.contracts.utils.getValueBy
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

@Cached
@Introspected
@Serializable
data class Region(
    val id: Long,
    val parentId: Long?,
    val country: String,
    val depth: Int
) {

    fun geometry(env: DataFetchingEnvironment): CompletableFuture<TMultiPolygon> {
        return env.getValueBy(RegionQueries::loadGeometriesByRegionId, id)
    }

    fun centroid(env: DataFetchingEnvironment): CompletableFuture<TPoint> {
        return env.getValueBy(RegionQueries::loadCentroidsByRegionId, id)
    }

    fun name(env: DataFetchingEnvironment, lang: String? = null): CompletableFuture<String> {
        return env.getValueBy(RegionQueries::loadRegionsName, id, lang)
    }

    fun parent(env: DataFetchingEnvironment): CompletableFuture<Region?> {
        return if (parentId == null) completedFuture(null) else {
            env.byId(parentId)
        }
    }

    fun children(env: DataFetchingEnvironment): CompletableFuture<List<Region>> {
        return env.getValueBy(RegionQueries::loadRegionByParentIds, id)
    }
}
