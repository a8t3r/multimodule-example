package io.eordie.multimodule.contracts.organization.models.invitation

import io.eordie.multimodule.contracts.basic.filters.Direction
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class InvitationFilter(
    val direction: Direction? = null,
    val organizationId: UUIDLiteralFilter? = null,
    val email: StringLiteralFilter? = null,
    val status: InvitationStatusFilter? = null
)
