@file:Suppress("SpellCheckingInspection")

package io.eordie.multimodule.contracts.basic.geometry

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Serializable
sealed interface SpatialReference {
    val srid: Int
}

@Introspected
@Serializable
data class TCoordinate(val x: Double, val y: Double)

@Introspected
@Serializable
data class TPoint(override val srid: Int, val x: Double, val y: Double) : SpatialReference

@Introspected
@Serializable
data class TLine(override val srid: Int, val coordinates: List<TCoordinate>) : SpatialReference

@Introspected
@Serializable
data class TPolygon(
    override val srid: Int,
    val exterior: List<TPoint>,
    val holes: List<List<TPoint>>
) : SpatialReference

@Introspected
@Serializable
data class TMultiPolygon(
    override val srid: Int,
    val polygons: List<TPolygon>
) : SpatialReference

@Introspected
@Serializable
data class TBox(
    val min: TPoint,
    val max: TPoint
)
