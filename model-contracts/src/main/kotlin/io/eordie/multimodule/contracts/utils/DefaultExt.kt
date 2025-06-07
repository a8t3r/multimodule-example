package io.eordie.multimodule.contracts.utils

import io.micronaut.core.beans.BeanIntrospection
import kotlin.reflect.KClass

inline fun <reified T> safeCast(input: Any?): T = requireNotNull(input as? T)

@Suppress("UNCHECKED_CAST")
fun <T> Any?.uncheckedCast(): T = requireNotNull(this as? T)

fun <T> getIntrospection(targetClass: KClass<*>): BeanIntrospection<T> {
    return beanIntrospections.getOrPut(targetClass) {
        BeanIntrospection.getIntrospection(targetClass.java)
    }.uncheckedCast()
}

val beanIntrospections = mutableMapOf<KClass<*>, BeanIntrospection<*>>()

inline fun <reified T> T?.orDefault(): T {
    return if (this !== null) this else {
        val introspection = getIntrospection<T>(T::class)
        introspection.instantiate() as T
    }
}
