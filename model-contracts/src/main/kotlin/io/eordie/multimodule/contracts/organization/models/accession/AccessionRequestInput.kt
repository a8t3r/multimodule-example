package io.eordie.multimodule.contracts.organization.models.accession

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class AccessionRequestInput(
    val id: UuidStr?,
    val vat: String
)
