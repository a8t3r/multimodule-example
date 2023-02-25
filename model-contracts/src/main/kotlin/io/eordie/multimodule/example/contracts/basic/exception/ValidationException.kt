package io.eordie.multimodule.example.contracts.basic.exception

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ValidationError(
    val dataPath: String,
    val message: String
)

@Serializable
class ValidationException(
    val errors: List<ValidationError>
) : BaseRuntimeException() {
    override val message = "validation errors occurred"
    override val extensions: Map<String, @Contextual Any> = mapOf(
        "constraints" to errors.associateBy({ it.dataPath }, { it.message })
    )
}
