package io.eordie.multimodule.example.contracts.basic.exception

import kotlinx.serialization.Serializable

@Serializable
data class ExceptionDefinition(
    val type: String?,
    val message: String?
) {
    constructor(ex: Throwable) : this(ex::class.simpleName, ex.message)
}
