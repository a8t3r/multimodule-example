package io.eordie.multimodule.contracts.organization.models.extra

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationPublicInformationInput(
    val vat: String? = null,
    val query: String? = null,
    val organizationId: UuidStr? = null
)
