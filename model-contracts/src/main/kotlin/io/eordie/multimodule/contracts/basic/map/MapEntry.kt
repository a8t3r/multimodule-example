package io.eordie.multimodule.contracts.basic.map

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Serializable
@Introspected
data class MapEntry<K, V>(val key: K, val value: V)

typealias MapEntries<K, V> = List<MapEntry<K, V>>

fun <K, V> MapEntries<K, V>.toMap(): Map<K, V> {
    return this.fold(mutableMapOf()) { acc, (key, value) ->
        acc[key] = value
        acc
    }
}

fun <K, V> MapEntries<K, V>.toMultimap(): Multimap<K, V> {
    return this.fold(LinkedHashMultimap.create()) { acc, (key, value) ->
        acc.put(key, value)
        acc
    }
}

fun <K, V> Map<K, V>.toEntries(): MapEntries<K, V> {
    return this.entries.fold(mutableListOf()) { acc, (key, value) ->
        acc.add(MapEntry(key, value))
        acc
    }
}
