package io.eordie.multimodule.contracts.organization.models.invitation

import io.eordie.multimodule.contracts.basic.filters.EnumLiteralFilter
import kotlinx.serialization.Serializable

@Serializable
data class InvitationStatusFilter(
    override val eq: InvitationStatus? = null,
    override val ne: InvitationStatus? = null,
    override val of: List<InvitationStatus>? = null,
    override val nof: List<InvitationStatus>? = null,
    override val nil: Boolean? = null
) : EnumLiteralFilter<InvitationStatus>
