package io.eordie.multimodule.contracts.organization.models

import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationsFilter(
    val id: UUIDLiteralFilter? = null,
    val name: StringLiteralFilter? = null,
    val createdBy: UUIDLiteralFilter? = null,
    val members: UsersFilter? = null,
    val domains: DomainFilter? = null
)
