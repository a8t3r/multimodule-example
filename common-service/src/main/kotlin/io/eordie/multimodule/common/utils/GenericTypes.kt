package io.eordie.multimodule.common.utils

import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.jvm.jvmErasure

object GenericTypes {

    private val cache = mutableMapOf<Pair<KClass<*>, KClass<*>>, KClass<*>>()

    // no cache support
    fun <T : Any> getTypeArguments(instance: Any, targetType: KClass<T>): List<KClass<*>> {
        return instance::class.allSupertypes
            .single { it.classifier == targetType }
            .arguments.map { requireNotNull(it.type?.jvmErasure) }
    }

    fun <T : Any> getTypeArgument(instance: Any, targetType: KClass<T>): KClass<*> {
        return cache.getOrPut(instance::class to targetType) {
            val result = instance::class.allSupertypes
                .single { it.classifier == targetType }
                .arguments.single().type?.jvmErasure

            requireNotNull(result)
        }
    }
}
