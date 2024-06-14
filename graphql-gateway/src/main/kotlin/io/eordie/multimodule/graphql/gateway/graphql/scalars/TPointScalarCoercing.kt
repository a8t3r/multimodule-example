package io.eordie.multimodule.graphql.gateway.graphql.scalars

import graphql.schema.GraphQLScalarType
import io.eordie.multimodule.common.utils.JtsUtils
import io.eordie.multimodule.contracts.basic.geometry.GeoJsonPoint
import io.eordie.multimodule.contracts.basic.geometry.TPoint

class TPointScalarCoercing : AbstractJsonScalarCoercing<TPoint, GeoJsonPoint>(
    TPoint::class,
    GeoJsonPoint::class
) {

    companion object {
        val Scalar: GraphQLScalarType = GraphQLScalarType.newScalar()
            .name("TPoint")
            .description("Geo json point")
            .coercing(TPointScalarCoercing())
            .build()
    }

    override fun fromInput(input: TPoint): GeoJsonPoint {
        val coordinates = ArrayList<Double>().apply {
            add(input.x)
            add(input.y)
        }

        return GeoJsonPoint("Point", coordinates, input.srid)
    }

    override fun toInput(output: GeoJsonPoint): TPoint {
        require(output.type == "Point")
        require(!output.coordinates.isNullOrEmpty())

        val coordinates = requireNotNull(output.coordinates)
        return TPoint(
            output.srid ?: JtsUtils.GOOGLE_MERCATOR,
            coordinates[0],
            coordinates[1]
        )
    }
}
