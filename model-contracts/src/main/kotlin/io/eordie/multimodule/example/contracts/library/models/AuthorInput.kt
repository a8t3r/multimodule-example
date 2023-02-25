package io.eordie.multimodule.example.contracts.library.models

import io.eordie.multimodule.example.contracts.InputOnly
import io.eordie.multimodule.example.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@InputOnly
@Serializable
@Introspected
data class AuthorInput(
    val id: UuidStr? = null,
    val firstName: String? = null,
    val lastName: String? = null
)
