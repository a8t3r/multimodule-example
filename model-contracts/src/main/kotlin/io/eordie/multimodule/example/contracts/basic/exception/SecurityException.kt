package io.eordie.multimodule.example.contracts.basic.exception

import kotlinx.serialization.Contextual

open class SecurityException : BaseRuntimeException() {
    override val extensions: Map<String, @Contextual Any> = emptyMap()
}
