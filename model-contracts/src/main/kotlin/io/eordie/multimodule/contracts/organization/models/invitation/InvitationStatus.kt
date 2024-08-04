package io.eordie.multimodule.contracts.organization.models.invitation

import kotlinx.serialization.Serializable

@Serializable
enum class InvitationStatus {
    CREATED,
    PENDING,
    ACCEPTED
}
