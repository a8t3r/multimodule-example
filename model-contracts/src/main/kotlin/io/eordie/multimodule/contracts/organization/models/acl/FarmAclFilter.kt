package io.eordie.multimodule.contracts.organization.models.acl

import io.eordie.multimodule.contracts.basic.filters.LongNumericFilter
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.organization.models.Direction
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class FarmAclFilter(
    val direction: Direction? = null,
    val farmId: UUIDLiteralFilter? = null,
    val fieldId: UUIDLiteralFilter? = null,
    val regionId: LongNumericFilter? = null,
    val organization: OrganizationsFilter? = null,
    val relatedOrganizationId: UUIDLiteralFilter? = null,
    val farmOwnerOrganization: OrganizationsFilter? = null
)
