package io.eordie.multimodule.organization.management.models

import java.util.*

enum class OperationType {
    CREATE, DELETE, UPDATE, ACTION
}

data class AuthDetails(
    val userId: UUID,
    val username: String
)

data class KeycloakEvent(
    val realmId: String,
    val type: String,
    val operationType: OperationType?,
    val resourcePath: String?,
    val resourceType: String?,
    val details: AuthDetails?
) {
    fun isRegistration() = type == "access.REGISTER"
    fun isLogin() = type == "access.LOGIN"
    fun isLogout() = type == "access.LOGOUT"
}
