package io.eordie.multimodule.contracts.basic.event

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.reflect.KProperty1

@Serializable
data class Difference(
    val set: MutableList<String> = mutableListOf(),
    val unset: MutableList<String> = mutableListOf(),
    val updated: MutableList<String> = mutableListOf(),
) {
    fun hasAnyChanges() = set.isNotEmpty() || unset.isNotEmpty() || updated.isNotEmpty()
}

@Serializable
data class MutationEvent<T>(
    val id: String,
    val old: @Contextual T?,
    val new: @Contextual T?,
    val difference: Difference?
) {
    fun isCreated() = old == null && new != null
    fun isDeleted() = old != null && new == null
    fun isUpdated() = old != null && new != null

    fun getPrevious(): T = requireNotNull(old)
    fun getActual(): T = requireNotNull(new)

    fun hasChangesOnAny(vararg properties: KProperty1<T, *>): Boolean {
        return if (difference == null) false else {
            val other = properties.map { it.name }.toSet()
            (difference.set + difference.unset + difference.updated).intersect(other).isNotEmpty()
        }
    }
}
