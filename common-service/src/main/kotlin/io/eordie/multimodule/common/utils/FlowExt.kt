package io.eordie.multimodule.common.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.fold
import kotlin.reflect.KProperty1

suspend fun <T, P, V> Flow<T>.associateBy(property: KProperty1<T, P>, valueMapper: (T) -> V): Map<P, V> =
    associateBy({ property.invoke(it) }, valueMapper)

suspend fun <T, P, V> Flow<T>.associateById(ids: List<P>, idMapper: (T) -> P, valueMapper: (T) -> V): Map<P, List<V>> =
    associateFlattenById(ids, idMapper) { listOf(valueMapper(it)) }

suspend fun <T, P, V> Flow<T>.associateByIds(ids: List<P>, property: KProperty1<T, List<P>>, valueMapper: (T) -> V): Map<P, List<V>> =
    this.associateByIds(ids, { property.get(it) }, { valueMapper(it) })

suspend fun <T, P, V> Flow<T>.associateByIds(ids: List<P>, idMapper: (T) -> List<P>, valueMapper: (T) -> V): Map<P, List<V>> =
    associateFlattenByIds(ids, idMapper) { listOf(valueMapper(it)) }

suspend fun <T, P, V> Flow<T>.associateFlattenByIds(ids: List<P>, idMapper: (T) -> List<P>, valueMapper: (T) -> List<V>): Map<P, List<V>> {
    val index = this.flatMapConcat { value ->
        idMapper(value).map { it to valueMapper(value) }.asFlow()
    }.fold(mutableMapOf<P, MutableSet<V>>()) { acc, (key, value) ->
        acc.getOrPut(key) { mutableSetOf() }.addAll(value)
        acc
    }

    return ids.fold(mutableMapOf()) { acc, id ->
        acc[id] = index[id]?.toList().orEmpty()
        acc
    }
}

suspend fun <T, P, V> Flow<T>.associateFlattenById(ids: List<P>, idMapper: (T) -> P, valueMapper: (T) -> List<V>): Map<P, List<V>> {
    val index = this.associateBy(idMapper, valueMapper)
    return ids.fold(mutableMapOf()) { acc, id ->
        acc[id] = index[id].orEmpty()
        acc
    }
}

private suspend fun <T, P, V> Flow<T>.associateBy(idMapper: (T) -> P, valueMapper: (T) -> V): Map<P, V> {
    return this.fold(mutableMapOf()) { acc, value ->
        acc[idMapper(value)] = valueMapper(value)
        acc
    }
}
