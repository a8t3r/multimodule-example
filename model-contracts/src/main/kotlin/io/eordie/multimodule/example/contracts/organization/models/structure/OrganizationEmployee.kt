package io.eordie.multimodule.example.contracts.organization.models.structure

import io.eordie.multimodule.example.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationEmployee(
    val userId: UuidStr,
    val organizationId: UuidStr,
    val departmentId: UuidStr,
    val positionId: UuidStr
)
