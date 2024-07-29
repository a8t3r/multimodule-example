package io.eordie.multimodule.common.utils

import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.jvm.jvmErasure

object GenericTypes {

    private val cache = mutableMapOf<Pair<KClass<*>, KClass<*>>, List<KClass<*>>>()

    fun <T : Any> getTypeArguments(instance: Any, targetType: KClass<T>): List<KClass<*>> {
        return getTypeArgumentsFromClass(instance::class, targetType)
    }

    fun <T : Any> getTypeArgument(instance: T, targetType: KClass<T>): KClass<*> {
        return getTypeArgumentFromClass(instance::class, targetType)
    }

    fun <T : Any> getTypeArgumentFromClass(instanceType: KClass<out T>, targetType: KClass<T>): KClass<*> {
        return getTypeArgumentsFromClass(instanceType, targetType).single()
    }

    private fun getTypeArgumentsFromClass(instanceType: KClass<*>, targetType: KClass<*>): List<KClass<*>> {
        return cache.getOrPut(instanceType to targetType) {
            instanceType.allSupertypes
                .single { it.classifier == targetType }
                .arguments.map { requireNotNull(it.type?.jvmErasure) }
        }
    }
}
