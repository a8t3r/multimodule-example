package io.eordie.multimodule.contracts.organization.models.invitation

import io.eordie.multimodule.contracts.utils.Roles
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class InvitationOptions(
    val roles: List<Roles>? = emptyList(),
    val failIfUserAlreadyExists: Boolean? = false,
    val invitationEmail: Boolean? = false
)
