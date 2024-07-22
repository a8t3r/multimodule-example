package io.eordie.multimodule.contracts.basic.exception

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

    constructor(message: String) : this(listOf(ValidationError(".", message)))

    override val message get() = "validation errors occurred: $errors"

    override fun extensions(): Map<String, Any> = mapOf(
        "constraints" to errors.associateBy({ it.dataPath }, { it.message })
    )
}
