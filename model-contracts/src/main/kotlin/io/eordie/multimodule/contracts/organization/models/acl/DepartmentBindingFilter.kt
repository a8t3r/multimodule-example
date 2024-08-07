package io.eordie.multimodule.contracts.organization.models.acl

import io.eordie.multimodule.contracts.basic.filters.LongNumericFilter
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class DepartmentBindingFilter(
    val organizationId: UUIDLiteralFilter? = null,
    val farmOwnerOrganizationId: UUIDLiteralFilter? = null,
    val departmentId: UUIDLiteralFilter? = null,
    val farmId: UUIDLiteralFilter? = null,
    val regionId: LongNumericFilter? = null,
    val fieldId: UUIDLiteralFilter? = null
)
