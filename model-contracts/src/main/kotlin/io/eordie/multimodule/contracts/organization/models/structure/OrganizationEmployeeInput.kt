package io.eordie.multimodule.contracts.organization.models.structure

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationEmployeeInput(
    val userId: UuidStr,
    val departmentId: UuidStr,
    val positionId: UuidStr
)
