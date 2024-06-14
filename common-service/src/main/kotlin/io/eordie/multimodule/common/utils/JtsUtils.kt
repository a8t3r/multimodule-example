package io.eordie.multimodule.common.utils

import com.google.common.cache.CacheBuilder
import io.eordie.multimodule.contracts.basic.geometry.SpatialReference
import io.eordie.multimodule.contracts.basic.geometry.TCoordinate
import io.eordie.multimodule.contracts.basic.geometry.TLine
import io.eordie.multimodule.contracts.basic.geometry.TMultiPolygon
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.basic.geometry.TPolygon
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.CoordinateSequence
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.MultiPolygon
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.geom.PrecisionModel
import org.locationtech.jts.geom.impl.CoordinateArraySequence
import org.locationtech.jts.io.WKBReader
import org.locationtech.jts.io.WKBWriter
import org.locationtech.jts.io.WKTWriter
import java.util.*

fun TPoint.jts(): Point = JtsUtils.toPoint(this)

fun TLine.jts(): LineString = JtsUtils.toLineString(this)

fun TPolygon.jts(): Polygon = JtsUtils.toPolygon(this)

fun TMultiPolygon.jts(): MultiPolygon = JtsUtils.toMultiPolygon(this)

fun Polygon.tPolygon(ignoreHoles: Boolean = false): TPolygon {
    return JtsUtils.toTPolygon(this, ignoreHoles)
}

fun MultiPolygon.tMultiPolygon(): TMultiPolygon = JtsUtils.toTMultiPolygon(this)

fun LineString.tLine(): TLine = JtsUtils.toTLine(this)

fun Geometry.asEwkt(): String {
    return "SRID=${this.srid};${WKTWriter().write(this)}"
}

fun Geometry.asPostgis(): String {
    return "CAST('${this.asEwkt()}' AS geometry)"
}

@Suppress("TooManyFunctions")
object JtsUtils {

    // hash(wkb) -> TPolygon | TMultiPolygon
    private val tGeometryCache = CacheBuilder.newBuilder()
        .concurrencyLevel(5)
        .maximumSize(1000)
        .build<String, Any>()

    // hash(TPolygon | TMultiPolygon) -> wkb
    private val wkbCache = CacheBuilder.newBuilder()
        .concurrencyLevel(5)
        .maximumSize(1000)
        .build<SpatialReference, String>()

    const val WGS_84 = 4326
    const val GOOGLE_MERCATOR = 3857

    val wgs84 = GeometryFactory(PrecisionModel(), WGS_84)
    val mercator = GeometryFactory(PrecisionModel(), GOOGLE_MERCATOR)

    private val geometryFactories = Collections.synchronizedMap(HashMap<Int, GeometryFactory>())

    fun getGeometryFactory(srid: Int): GeometryFactory {
        return when (srid) {
            WGS_84 -> wgs84
            GOOGLE_MERCATOR -> mercator
            else -> {
                geometryFactories.computeIfAbsent(srid) { k -> GeometryFactory(PrecisionModel(), k) }
            }
        }
    }

    fun <T> fromWKBCached(wkb: String): T {
        return tGeometryCache.get(wkb) {
            val aux = WKBReader.hexToBytes(wkb)
            when (val geom: Geometry = WKBReader().read(aux)) {
                is Polygon -> geom.tPolygon()
                is MultiPolygon -> geom.tMultiPolygon()
                is LineString -> geom.tLine()
                else -> error("unknown geometry type: ${geom.geometryType}")
            }
        } as T
    }

    fun asWKBCached(g: TPolygon): String {
        return wkbCache.get(g) { WKBWriter.toHex(WKBWriter(2, true).write(g.jts())) }
    }

    fun asWKBCached(g: TMultiPolygon): String {
        return wkbCache.get(g) { WKBWriter.toHex(WKBWriter(2, true).write(g.jts())) }
    }

    fun asWKBCached(g: TLine): String {
        return wkbCache.get(g) { WKBWriter.toHex(WKBWriter(2, true).write(g.jts())) }
    }

    private fun toCoordinates(tPoints: List<TPoint>): CoordinateSequence {
        val coordinates = arrayOfNulls<Coordinate>(tPoints.size)
        for (i in tPoints.indices) {
            coordinates[i] = Coordinate(tPoints[i].x, tPoints[i].y)
        }
        return CoordinateArraySequence(coordinates)
    }

    fun toPoint(p: TPoint): Point {
        val gf = getGeometryFactory(p.srid)
        val exCoordinates = toCoordinates(listOf(p))
        return Point(exCoordinates, gf)
    }

    fun toTLine(line: LineString): TLine {
        val points = line.coordinates.map { TCoordinate(it.x, it.y) }
        return TLine(line.srid, points)
    }

    fun toLineString(line: TLine): LineString {
        assert(line.srid > 0) { "SRID must be set" }
        val gf = getGeometryFactory(line.srid)
        val points = line.coordinates.map { Coordinate(it.x, it.y) }.toTypedArray()
        return gf.createLineString(points)
    }

    fun toTMultiPolygon(mp: MultiPolygon): TMultiPolygon {
        require(mp.numGeometries > 0) { "polygons should be non empty" }

        val gf = getGeometryFactory(mp.srid)
        val polygons = (0 until mp.numGeometries)
            .map { mp.getGeometryN(it) as Polygon }
            .onEach { it.srid = gf.srid }
            .map { it.tPolygon() }

        return TMultiPolygon(gf.srid, polygons)
    }

    fun toMultiPolygon(mtp: TMultiPolygon): MultiPolygon {
        require(mtp.polygons.isNotEmpty()) { "polygons should be non empty" }

        val gf = getGeometryFactory(mtp.srid)
        val polygons = mtp.polygons.map { it.copy(srid = mtp.srid) }.map { toPolygon(it) }.toTypedArray()
        return MultiPolygon(polygons, gf)
    }

    fun toTPolygon(p: Polygon, ignoreHoles: Boolean = false): TPolygon {
        val (exterior, holes) = mutableListOf<TPoint>() to mutableListOf<List<TPoint>>()
        val tp = TPolygon(p.srid, exterior, holes)

        for (c in p.exteriorRing.coordinates) {
            exterior.add(TPoint(p.srid, c.x, c.y))
        }

        if (!ignoreHoles) {
            for (i in 0 until p.numInteriorRing) {
                val hole = mutableListOf<TPoint>()
                for (c in p.getInteriorRingN(i).coordinates) {
                    hole.add(TPoint(p.srid, c.x, c.y))
                }
                holes.add(hole)
            }
        }
        return tp
    }

    fun toPolygon(tp: TPolygon): Polygon {
        val gf = getGeometryFactory(tp.srid)
        val exCoordinates = toCoordinates(tp.exterior)

        val holes = arrayOfNulls<LinearRing>(tp.holes.size)
        for (h in 0 until tp.holes.size) {
            holes[h] = LinearRing(toCoordinates(tp.holes[h]), gf)
        }

        return Polygon(LinearRing(exCoordinates, gf), holes, gf)
    }
}
