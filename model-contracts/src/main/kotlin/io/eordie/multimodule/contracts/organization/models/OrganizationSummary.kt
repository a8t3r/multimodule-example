package io.eordie.multimodule.contracts.organization.models

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationSummary(
    val totalCount: Long,
    val organizationIds: List<UuidStr>
)
