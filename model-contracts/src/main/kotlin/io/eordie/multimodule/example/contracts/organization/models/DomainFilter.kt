package io.eordie.multimodule.example.contracts.organization.models

import io.eordie.multimodule.example.contracts.basic.filters.BooleanLiteralFilter
import io.eordie.multimodule.example.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.example.contracts.basic.filters.UUIDLiteralFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class DomainFilter(
    val id: UUIDLiteralFilter? = null,
    val domain: StringLiteralFilter? = null,
    val verified: BooleanLiteralFilter? = null
)
