package io.eordie.multimodule.example.contracts.basic.loader

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

interface EntityLoader<T : Any, ID> {
    suspend fun load(ids: List<ID>): Map<ID, T> = load(coroutineContext, ids)
    fun load(context: CoroutineContext, ids: List<ID>): Map<ID, T>
}
