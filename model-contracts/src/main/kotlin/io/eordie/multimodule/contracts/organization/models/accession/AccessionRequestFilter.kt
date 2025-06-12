package io.eordie.multimodule.contracts.organization.models.accession

import io.eordie.multimodule.contracts.basic.filters.EnumLiteralFilter
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class AccessionRequestFilter(
    val id: UUIDLiteralFilter? = null,
    val organizationId: UUIDLiteralFilter? = null,
    val status: EnumLiteralFilter<AccessionRequestStatus>? = null
)
