package io.eordie.multimodule.contracts.organization.models.structure

import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationPositionInput(
    val id: UuidStr?,
    val name: String,
    val roles: List<Roles>,
    val parentId: UuidStr?
)
