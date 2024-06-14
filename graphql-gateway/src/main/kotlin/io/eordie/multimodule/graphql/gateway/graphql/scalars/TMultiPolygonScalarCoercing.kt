package io.eordie.multimodule.graphql.gateway.graphql.scalars

import graphql.schema.GraphQLScalarType
import io.eordie.multimodule.common.utils.JtsUtils
import io.eordie.multimodule.contracts.basic.geometry.GeoJsonMultiPolygon
import io.eordie.multimodule.contracts.basic.geometry.TMultiPolygon

class TMultiPolygonScalarCoercing : AbstractJsonScalarCoercing<TMultiPolygon, GeoJsonMultiPolygon>(
    TMultiPolygon::class,
    GeoJsonMultiPolygon::class
) {

    companion object {
        val Scalar: GraphQLScalarType = GraphQLScalarType.newScalar()
            .name("TMultiPolygon")
            .description("Geo json multi polygon")
            .coercing(TMultiPolygonScalarCoercing())
            .build()
    }

    override fun fromInput(input: TMultiPolygon): GeoJsonMultiPolygon {
        val result = ArrayList<List<List<List<Double>>>>()
        input.polygons.forEach { polygon ->
            result.add(asList(polygon))
        }

        return GeoJsonMultiPolygon("MultiPolygon", result, input.srid)
    }

    override fun toInput(output: GeoJsonMultiPolygon): TMultiPolygon {
        require(output.type == "MultiPolygon")
        require(!output.coordinates.isNullOrEmpty())

        val srid = output.srid ?: JtsUtils.GOOGLE_MERCATOR
        val polygons = requireNotNull(output.coordinates).map { coordinates ->
            asPolygon(srid, coordinates)
        }

        return TMultiPolygon(srid, polygons)
    }
}
