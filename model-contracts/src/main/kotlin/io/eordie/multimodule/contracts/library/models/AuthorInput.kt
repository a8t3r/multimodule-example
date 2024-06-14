package io.eordie.multimodule.contracts.library.models

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Serializable
@Introspected
data class AuthorInput(
    val id: UuidStr? = null,
    val firstName: String? = null,
    val lastName: String? = null
)
