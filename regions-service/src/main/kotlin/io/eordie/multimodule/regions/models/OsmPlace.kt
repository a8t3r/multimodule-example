package io.eordie.multimodule.regions.models

import io.eordie.multimodule.contracts.basic.geometry.TMultiPolygon
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "placex")
interface OsmPlace {
    @Id
    val id: OsmPlaceId

    val geometry: TMultiPolygon

    val centroid: TPoint
}
