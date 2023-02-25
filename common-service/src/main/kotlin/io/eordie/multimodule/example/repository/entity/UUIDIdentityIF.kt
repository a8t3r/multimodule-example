package io.eordie.multimodule.example.repository.entity

import io.eordie.multimodule.example.repository.AuditableAware
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.MappedSuperclass
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import java.util.*

@MappedSuperclass
interface UUIDIdentityIF : AuditableAware {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generatorType = UUIDIdGenerator::class)
    val id: UUID
}
