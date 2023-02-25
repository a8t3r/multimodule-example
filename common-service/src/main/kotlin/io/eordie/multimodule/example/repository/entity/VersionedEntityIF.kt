package io.eordie.multimodule.example.repository.entity

import org.babyfish.jimmer.sql.MappedSuperclass
import org.babyfish.jimmer.sql.Version

@MappedSuperclass
interface VersionedEntityIF : CreatedAtIF {

    @Version
    val version: Int
}
