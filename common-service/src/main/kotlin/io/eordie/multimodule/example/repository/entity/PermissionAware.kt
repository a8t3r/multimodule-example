package io.eordie.multimodule.example.repository.entity

import kotlinx.serialization.Serializable
import org.babyfish.jimmer.sql.MappedSuperclass
import org.babyfish.jimmer.sql.Transient

@Serializable
enum class Permission {
    VIEW,
    MANAGE,
    PURGE
}

@MappedSuperclass
interface PermissionAware {
    @Transient
    val permissions: List<Permission>?
}
