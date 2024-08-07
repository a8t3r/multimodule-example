package io.eordie.multimodule.contracts.organization.models.acl

import io.eordie.multimodule.contracts.basic.BasePermission
import io.eordie.multimodule.contracts.basic.PermissionAware
import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class FarmAcl(
    val id: UuidStr,
    val organizationId: UuidStr,
    val roles: List<String>,
    val farmId: UuidStr,
    val fieldIds: List<UuidStr>? = null,
    override val permissions: List<BasePermission>
) : PermissionAware<BasePermission>
