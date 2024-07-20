package io.eordie.multimodule.contracts.library.models

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class BookInput(
    val id: UuidStr?,
    val name: String,
    val authors: List<AuthorInput>
)
