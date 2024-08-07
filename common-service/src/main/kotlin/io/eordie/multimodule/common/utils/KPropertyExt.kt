package io.eordie.multimodule.common.utils

import kotlin.reflect.KFunction

fun Collection<KFunction<*>>.like(that: KFunction<*>): KFunction<*>? {
    val simpleMatching = this.filter { it.name == that.name && it.parameters.size == that.parameters.size }

    // simple match by name and parameters size
    return if (simpleMatching.size <= 1) simpleMatching.firstOrNull() else {
        simpleMatching.firstOrNull {
            // additional match by parameter names
            it.parameters.zip(that.parameters).all { (a, b) -> a.name == b.name }
        }
    }
}
