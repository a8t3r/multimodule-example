package io.eordie.multimodule.example.contracts.library.models

import io.eordie.multimodule.example.contracts.InputOnly
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@InputOnly
@Introspected
@Serializable
data class BookInput(
    val name: String,
    val authors: List<AuthorInput>
)
