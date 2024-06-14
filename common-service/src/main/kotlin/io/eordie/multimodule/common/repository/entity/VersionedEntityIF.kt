package io.eordie.multimodule.common.repository.entity

import org.babyfish.jimmer.sql.MappedSuperclass
import org.babyfish.jimmer.sql.Version

@MappedSuperclass
interface VersionedEntityIF : CreatedAtIF {

    @Version
    val version: Int
}
