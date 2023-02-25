package io.eordie.multimodule.example.repository.entity

import org.babyfish.jimmer.sql.MappedSuperclass
import java.time.OffsetDateTime

@MappedSuperclass
interface CreatedAtIF {
    val createdAt: OffsetDateTime
}
