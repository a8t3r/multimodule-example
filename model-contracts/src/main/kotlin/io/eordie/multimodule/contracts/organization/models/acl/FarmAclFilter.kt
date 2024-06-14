package io.eordie.multimodule.contracts.organization.models.acl

import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class FarmAclFilter(
    val farmId: UUIDLiteralFilter? = null,
    val fieldId: UUIDLiteralFilter? = null,
    val organization: OrganizationsFilter? = null,
    val farmOwnerOrganization: OrganizationsFilter? = null
)
