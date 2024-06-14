package io.eordie.multimodule.common.rsocket.client

import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.Query
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

fun KClass<out Any>.isServiceInterface(): Boolean {
    return getServiceInterface() != null
}

fun KClass<out Any>.getServiceInterface(): KClass<*>? {
    return this.superclasses.firstOrNull {
        val parent = it.superclasses.firstOrNull()
        parent == Query::class || parent == Mutation::class
    }
}
