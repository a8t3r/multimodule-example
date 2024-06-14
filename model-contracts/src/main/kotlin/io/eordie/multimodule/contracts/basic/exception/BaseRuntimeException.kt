package io.eordie.multimodule.contracts.basic.exception

import kotlinx.serialization.Serializable

@Serializable
sealed class BaseRuntimeException : RuntimeException() {
    open fun extensions(): Map<String, Any> = emptyMap()
}
