package io.eordie.multimodule.regions.models

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Serialized
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "planet_osm_rels")
interface OsmRelationModel {
    @Id
    val id: Long

    @Serialized
    val tags: Map<String, String>
}
