package io.eordie.multimodule.common.rsocket.client

import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

fun KClass<out Any>.getServiceInterface(type: KClass<*>): KClass<*>? = this.superclasses
    .firstOrNull { parent ->
        parent.superclasses.any { it == type }
    }
