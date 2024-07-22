package io.eordie.multimodule.contracts.organization.models.structure

import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationDepartmentFilter(
    val id: UUIDLiteralFilter? = null,
    val name: StringLiteralFilter? = null,
    val organization: OrganizationsFilter? = null,
    val organizationId: UUIDLiteralFilter? = null
)
