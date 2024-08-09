package io.eordie.multimodule.contracts.organization.models.extra

import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationPublicInformation(
    val name: String,
    val kpp: String,
    val ogrn: String,
    val inn: String,
    val address: String,
    val location: TPoint?
)
