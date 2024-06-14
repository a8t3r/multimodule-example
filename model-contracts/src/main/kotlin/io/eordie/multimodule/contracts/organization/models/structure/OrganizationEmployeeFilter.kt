package io.eordie.multimodule.contracts.organization.models.structure

import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.contracts.organization.models.UsersFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationEmployeeFilter(
    val user: UsersFilter? = null,
    val userId: UUIDLiteralFilter? = null,
    val organizationId: UUIDLiteralFilter? = null,
    val organization: OrganizationsFilter? = null,
    val department: OrganizationDepartmentFilter? = null,
    val position: OrganizationPositionFilter? = null
)
