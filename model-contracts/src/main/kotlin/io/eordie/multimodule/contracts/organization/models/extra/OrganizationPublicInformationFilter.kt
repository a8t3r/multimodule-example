package io.eordie.multimodule.contracts.organization.models.extra

import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationPublicInformationFilter(
    val vat: StringLiteralFilter? = null
)
