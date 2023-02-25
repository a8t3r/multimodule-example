package io.eordie.multimodule.example.contracts.library.models

import io.eordie.multimodule.example.contracts.basic.filters.OffsetDateTimeLiteralFilter
import io.eordie.multimodule.example.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.example.contracts.basic.filters.UUIDLiteralFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Serializable
@Introspected
data class BooksFilter(
    val id: UUIDLiteralFilter? = null,
    val name: StringLiteralFilter? = null,
    val createdAt: OffsetDateTimeLiteralFilter? = null,
    val updatedAt: OffsetDateTimeLiteralFilter? = null,
    val authorIds: UUIDLiteralFilter? = null,
    val authors: AuthorsFilter? = null
)
