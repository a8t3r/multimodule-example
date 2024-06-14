package io.eordie.multimodule.contracts.basic.loader

interface EntityLoader<T : Any, ID> {
    suspend fun load(ids: List<ID>): Map<ID, T>
}
