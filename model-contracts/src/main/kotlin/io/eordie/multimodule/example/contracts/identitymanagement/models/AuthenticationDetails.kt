package io.eordie.multimodule.example.contracts.identitymanagement.models

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class AuthenticationDetails(
    val userId: String?,
    val username: String? = null,
    val roles: List<String> = emptyList(),
    val active: Boolean = false,
    val emailVerified: Boolean = false
) {
    val authenticated: Boolean = userId != null
}
