package io.eordie.multimodule.common.repository.entity

import org.babyfish.jimmer.sql.MappedSuperclass
import java.time.OffsetDateTime

@MappedSuperclass
interface CreatedAtIF {
    val createdAt: OffsetDateTime
}
