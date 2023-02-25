package io.eordie.multimodule.example.repository.entity

import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.MappedSuperclass
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator

@MappedSuperclass
interface UUIDStringIdentityIF : CreatedAtIF, UpdatedAtIF, VersionedEntityIF, DeletedIF {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generatorType = UUIDIdGenerator::class)
    val id: String
}
