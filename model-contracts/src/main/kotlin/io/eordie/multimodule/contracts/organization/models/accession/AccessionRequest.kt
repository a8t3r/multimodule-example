package io.eordie.multimodule.contracts.organization.models.accession

import io.eordie.multimodule.contracts.AuditLog
import io.eordie.multimodule.contracts.organization.models.User
import io.eordie.multimodule.contracts.utils.OffsetDateTimeStr
import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class AccessionRequest(
    val id: UuidStr,
    val vat: String,
    val initiatedBy: User,
    val processedBy: User?,
    val status: AccessionRequestStatus,
    val rejectionMessage: String?,
    override val createdAt: OffsetDateTimeStr,
    override val updatedAt: OffsetDateTimeStr
) : AuditLog
