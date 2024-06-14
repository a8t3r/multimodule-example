package io.eordie.multimodule.graphql.gateway.graphql.scalars

import graphql.schema.GraphQLScalarType
import io.eordie.multimodule.common.utils.JtsUtils
import io.eordie.multimodule.contracts.basic.geometry.GeoJsonPolygon
import io.eordie.multimodule.contracts.basic.geometry.TPolygon

class TPolygonScalarCoercing : AbstractJsonScalarCoercing<TPolygon, GeoJsonPolygon>(
    TPolygon::class,
    GeoJsonPolygon::class
) {

    companion object {
        val Scalar: GraphQLScalarType = GraphQLScalarType.newScalar()
            .name("TPolygon")
            .description("Geo json polygon")
            .coercing(TPolygonScalarCoercing())
            .build()
    }

    override fun fromInput(input: TPolygon): GeoJsonPolygon {
        return GeoJsonPolygon("Polygon", asList(input), input.srid)
    }

    override fun toInput(output: GeoJsonPolygon): TPolygon {
        require(output.type == "Polygon")
        require(!output.coordinates.isNullOrEmpty())

        val srid = output.srid ?: JtsUtils.GOOGLE_MERCATOR
        return asPolygon(srid, requireNotNull(output.coordinates))
    }
}
