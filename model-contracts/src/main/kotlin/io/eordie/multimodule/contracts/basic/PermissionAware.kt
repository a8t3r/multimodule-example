package io.eordie.multimodule.contracts.basic

import kotlinx.serialization.Serializable

@Serializable
enum class Permission {
    VIEW,
    MANAGE,
    PURGE
}

interface PermissionAware {
    val permissions: List<Permission>
}
