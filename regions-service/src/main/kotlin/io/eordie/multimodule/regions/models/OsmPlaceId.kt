package io.eordie.multimodule.regions.models

import org.babyfish.jimmer.sql.Embeddable

@Embeddable
interface OsmPlaceId {
    val osmId: Long

    val osmType: OsmType
}
