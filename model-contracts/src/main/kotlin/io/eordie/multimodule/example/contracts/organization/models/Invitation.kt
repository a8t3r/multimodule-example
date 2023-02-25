package io.eordie.multimodule.example.contracts.organization.models

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class Invitation(
    val email: String,
    val send: Boolean,
    val roles: List<String>
)
