package io.eordie.multimodule.common.repository.entity

import io.eordie.multimodule.common.repository.entity.PermissionAwareIFProps.PERMISSIONS
import io.eordie.multimodule.contracts.basic.Permission
import org.babyfish.jimmer.sql.MappedSuperclass
import org.babyfish.jimmer.sql.Transient

@MappedSuperclass
interface PermissionAwareIF {
    @Transient
    val permissions: List<Permission>?

    fun loadedPermissions(): List<Permission> {
        return if (!PERMISSIONS.isLoaded(this)) emptyList() else {
            permissions.orEmpty()
        }
    }
}
