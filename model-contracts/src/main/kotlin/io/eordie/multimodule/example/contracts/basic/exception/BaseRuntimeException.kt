package io.eordie.multimodule.example.contracts.basic.exception

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
abstract class BaseRuntimeException : RuntimeException() {
    abstract val extensions: Map<String, @Contextual Any>
}
