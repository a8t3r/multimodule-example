package io.eordie.multimodule.common.rsocket.client

import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

fun KClass<out Any>.getServiceInterface(type: KClass<*>): KClass<*>? = sequenceOf(*this.superclasses.toTypedArray())
    .flatMap { listOf(it) + it.superclasses } // aop proxy -> controller bean -> interface
    .firstOrNull { parent ->
        parent.superclasses.any { it == type }
    }
