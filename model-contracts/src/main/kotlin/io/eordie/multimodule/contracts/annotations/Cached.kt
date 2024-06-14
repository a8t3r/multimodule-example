package io.eordie.multimodule.contracts.annotations

import kotlin.reflect.KClass

annotation class Cached(
    val value: KClass<out Any> = Unit::class
)
