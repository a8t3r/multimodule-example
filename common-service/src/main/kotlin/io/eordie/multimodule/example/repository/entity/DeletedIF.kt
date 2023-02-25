package io.eordie.multimodule.example.repository.entity

import org.babyfish.jimmer.sql.LogicalDeleted
import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface DeletedIF {
    @LogicalDeleted("true")
    val deleted: Boolean
}
