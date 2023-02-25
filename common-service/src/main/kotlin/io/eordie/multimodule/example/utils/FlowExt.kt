package io.eordie.multimodule.example.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.fold
import kotlin.reflect.KProperty1

suspend fun <T, P> Flow<T>.associateBy(property: KProperty1<T, P>): Map<P, T> = associateBy { property.invoke(it) }

suspend fun <T, P> Flow<T>.associateBy(idMapper: (T) -> P): Map<P, T> = associateBy(idMapper) { it }

suspend fun <T, P, V> Flow<T>.associateBy(property: KProperty1<T, P>, valueMapper: (T) -> V): Map<P, V> =
    associateBy({ property.invoke(it) }, valueMapper)

suspend fun <T, P, V> Flow<T>.associateBy(idMapper: (T) -> P, valueMapper: (T) -> V): Map<P, V> {
    return this.fold(mutableMapOf()) { acc, value ->
        acc[idMapper(value)] = valueMapper(value)
        acc
    }
}
