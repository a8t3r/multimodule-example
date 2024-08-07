package io.eordie.multimodule.common.utils

import kotlin.reflect.KFunction
import kotlin.reflect.KProperty0

fun <T> KProperty0<T>.getIfPresent(): T? = kotlin.runCatching { get() }.getOrNull()

private fun Collection<KFunction<*>>.like(name: String, parameterSize: Int): KFunction<*>? =
    this.singleOrNull { it.name == name && it.parameters.size == parameterSize }

fun Collection<KFunction<*>>.like(that: KFunction<*>): KFunction<*> =
    requireNotNull(like(that.name, that.parameters.size))

fun Collection<KFunction<*>>.matching(that: KFunction<*>): KFunction<*>? =
    like(that.name, that.parameters.size)

