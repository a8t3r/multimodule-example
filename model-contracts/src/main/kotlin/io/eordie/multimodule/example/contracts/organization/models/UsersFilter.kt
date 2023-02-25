package io.eordie.multimodule.example.contracts.organization.models

import io.eordie.multimodule.example.contracts.basic.filters.BooleanLiteralFilter
import io.eordie.multimodule.example.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.example.contracts.basic.filters.UUIDLiteralFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class UsersFilter(
    val id: UUIDLiteralFilter? = null,
    val enabled: BooleanLiteralFilter? = null,
    val firstName: StringLiteralFilter? = null,
    val lastName: StringLiteralFilter? = null,
    val email: StringLiteralFilter? = null,
    val emailVerified: BooleanLiteralFilter? = null,
    val organization: OrganizationsFilter? = null,
    val phoneNumber: StringLiteralFilter? = null,
    val phoneNumberVerified: BooleanLiteralFilter? = null
)
