package io.eordie.multimodule.contracts.basic.loader

import io.eordie.multimodule.contracts.basic.Permission

interface EntityLoader<T : Any, ID> {
    suspend fun load(ids: List<ID>): Map<ID, T>
    suspend fun loadPermissions(ids: List<ID>): Map<ID, List<Permission>>
}
