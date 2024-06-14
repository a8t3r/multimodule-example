package io.eordie.multimodule.contracts.organization.models.acl

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class FarmAclInput(
    val id: UuidStr?,
    val organisationId: UuidStr,
    val farmId: UuidStr,
    val roles: List<String>,
    val fieldIds: List<UuidStr>? = null
)
