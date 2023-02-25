package io.eordie.multimodule.example.utils

import kotlin.reflect.KProperty0

fun <T> KProperty0<T>.getIfPresent(): T? = kotlin.runCatching { get() }.getOrNull()
