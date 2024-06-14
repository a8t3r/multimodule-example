package io.eordie.multimodule.contracts.organization.models

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class UserInput(
    val firstName: String,
    val lastName: String,
    val email: String
)
