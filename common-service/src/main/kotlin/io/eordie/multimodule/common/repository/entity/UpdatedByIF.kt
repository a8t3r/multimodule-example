package io.eordie.multimodule.common.repository.entity

import org.babyfish.jimmer.sql.MappedSuperclass
import java.util.*

@MappedSuperclass
interface UpdatedByIF {
    val updatedBy: UUID
}
