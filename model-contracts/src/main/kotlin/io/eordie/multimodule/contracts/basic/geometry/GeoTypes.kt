package io.eordie.multimodule.contracts.basic.geometry

import kotlinx.serialization.Serializable

@Serializable
data class GeoJsonPoint(var type: String?, var coordinates: List<Double>?, val srid: Int?)

@Serializable
data class GeoJsonPolygon(var type: String?, var coordinates: List<List<List<Double>>>?, val srid: Int?)

@Serializable
data class GeoJsonMultiPolygon(var type: String?, var coordinates: List<List<List<List<Double>>>>?, val srid: Int?)
