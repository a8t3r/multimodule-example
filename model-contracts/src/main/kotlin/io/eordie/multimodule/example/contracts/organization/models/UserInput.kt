package io.eordie.multimodule.example.contracts.organization.models

import io.eordie.multimodule.example.contracts.InputOnly
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
@InputOnly
data class UserInput(
    val firstName: String,
    val lastName: String,
    val email: String
)
