package io.eordie.multimodule.example.rsocket.client

import io.eordie.multimodule.example.contracts.Mutation
import io.eordie.multimodule.example.contracts.Query
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
