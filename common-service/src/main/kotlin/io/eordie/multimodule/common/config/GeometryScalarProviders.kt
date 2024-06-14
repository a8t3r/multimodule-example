package io.eordie.multimodule.common.config

import io.eordie.multimodule.common.utils.JtsUtils
import io.eordie.multimodule.contracts.basic.geometry.SpatialReference
import io.eordie.multimodule.contracts.basic.geometry.TLine
import io.eordie.multimodule.contracts.basic.geometry.TMultiPolygon
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.basic.geometry.TPolygon
import org.babyfish.jimmer.sql.runtime.ScalarProvider
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.impl.CoordinateArraySequence
import org.locationtech.jts.io.WKBReader
import org.locationtech.jts.io.WKBWriter
import org.postgresql.util.PGobject

open class TPolygonProvider : ScalarProvider<TPolygon?, PGobject> {
    override fun toScalar(sqlValue: PGobject): TPolygon? {
        val value = sqlValue.value ?: return null
        return JtsUtils.fromWKBCached<TPolygon>(value)
    }

    override fun toSql(scalarValue: TPolygon): PGobject {
        return PGobject().apply {
            this.type = "geometry"
            this.value = JtsUtils.asWKBCached(scalarValue)
        }
    }
}

open class TMultiPolygonProvider : ScalarProvider<TMultiPolygon?, PGobject> {
    override fun toScalar(sqlValue: PGobject): TMultiPolygon? {
        val value = sqlValue.value ?: return null
        return when (val reference = JtsUtils.fromWKBCached<SpatialReference>(value)) {
            is TMultiPolygon -> reference
            is TPolygon -> TMultiPolygon(reference.srid, listOf(reference))
            else -> error("unsupported reference type: ${reference.javaClass}")
        }
    }

    override fun toSql(scalarValue: TMultiPolygon): PGobject {
        return PGobject().apply {
            this.type = "geometry"
            this.value = JtsUtils.asWKBCached(scalarValue)
        }
    }
}

open class TLineProvider : ScalarProvider<TLine?, PGobject> {
    override fun toScalar(sqlValue: PGobject): TLine? {
        val value = sqlValue.value ?: return null
        return JtsUtils.fromWKBCached<TLine>(value)
    }

    override fun toSql(scalarValue: TLine): PGobject {
        return PGobject().apply {
            this.type = "geometry"
            this.value = JtsUtils.asWKBCached(scalarValue)
        }
    }
}

open class TPointProvider : ScalarProvider<TPoint?, PGobject> {
    override fun toScalar(sqlValue: PGobject): TPoint? {
        val value = sqlValue.value ?: return null
        val point = WKBReader().read(WKBReader.hexToBytes(value)) as Point
        return TPoint(point.srid, point.x, point.y)
    }

    override fun toSql(scalarValue: TPoint): PGobject {
        return PGobject().apply {
            val coordinates = arrayOf(Coordinate(scalarValue.x, scalarValue.y))
            val point = Point(CoordinateArraySequence(coordinates), JtsUtils.getGeometryFactory(scalarValue.srid))

            this.type = "geometry"
            this.value = WKBWriter.toHex(WKBWriter(2, true).write(point))
        }
    }
}
