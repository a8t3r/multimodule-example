package io.eordie.multimodule.contracts.organization.models.invitation

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class InvitationInput(
    val id: UuidStr? = null,
    val email: String,
    val positionId: UuidStr? = null,
    val departmentId: UuidStr? = null
)
