package io.eordie.multimodule.example.contracts.organization.models.structure

import io.eordie.multimodule.example.contracts.InputOnly
import io.eordie.multimodule.example.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@InputOnly
@Introspected
@Serializable
data class OrganizationPositionInput(
    val id: UuidStr?,
    val name: String,
    val roles: List<String>,
    val parentId: UuidStr?
)
