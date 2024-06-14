package io.eordie.multimodule.contracts.library.models

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class BookInput(
    val name: String,
    val authors: List<AuthorInput>
)
