package io.eordie.multimodule.contracts.basic.exception

import kotlinx.coroutines.CopyableThrowable
import kotlinx.serialization.Serializable

@Serializable
sealed class BaseRuntimeException : RuntimeException(), CopyableThrowable<BaseRuntimeException> {
    open fun extensions(): Map<String, Any> = emptyMap()
}
