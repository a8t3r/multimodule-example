package io.eordie.multimodule.regions.models

import org.babyfish.jimmer.sql.EnumType

@EnumType(value = EnumType.Strategy.NAME)
enum class OsmType {
    N, R, W
}
