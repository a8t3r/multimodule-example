package io.eordie.multimodule.contracts.organization

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationInput(
    val id: UuidStr?,
    val name: String,
    val displayName: String? = null
)
