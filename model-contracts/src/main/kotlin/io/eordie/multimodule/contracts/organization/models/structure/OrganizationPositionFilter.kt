package io.eordie.multimodule.contracts.organization.models.structure

import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationPositionFilter(
    val name: StringLiteralFilter? = null,
    val parentId: UUIDLiteralFilter? = null,
    val organizationId: UUIDLiteralFilter? = null
)
