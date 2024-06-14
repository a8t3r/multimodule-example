package io.eordie.multimodule.contracts.organization.models.structure

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationDepartmentInput(
    val id: UuidStr?,
    val name: String
)
