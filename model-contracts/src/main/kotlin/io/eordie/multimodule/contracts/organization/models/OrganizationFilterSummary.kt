package io.eordie.multimodule.contracts.organization.models

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationFilterSummary(
    val totalCount: Long,
    val organizationIds: List<UuidStr>,
    val domainIds: List<UuidStr>,
    val userIds: List<UuidStr>
)
