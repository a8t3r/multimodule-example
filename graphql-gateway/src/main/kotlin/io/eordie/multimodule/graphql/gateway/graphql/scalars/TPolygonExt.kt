package io.eordie.multimodule.graphql.gateway.graphql.scalars

import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.basic.geometry.TPolygon

fun asList(polygon: TPolygon): List<List<List<Double>>> {
    val coordinates = ArrayList<List<List<Double>>>()
    coordinates.add(polygon.exterior.map { listOf(it.x, it.y) })
    for (hole in polygon.holes) {
        coordinates.add(hole.map { listOf(it.x, it.y) })
    }

    return coordinates
}

fun asPolygon(srid: Int, coordinates: List<List<List<Double>>>): TPolygon {
    val exterior = coordinates.first().map { TPoint(srid, it[0], it[1]) }
    val holes = mutableListOf<List<TPoint>>()

    if (coordinates.size > 1) {
        for (i in 1 until coordinates.size) {
            holes.add(coordinates[i].map { TPoint(srid, it[0], it[1]) })
        }
    }

    return TPolygon(srid, exterior, holes)
}
