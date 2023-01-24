package io.eordie.multimodule.example.gateway.security

class KeycloakUser {
    var sub: String? = null
    var username: String? = null
    var roles: List<String>? = null
    var emailVerified: Boolean = false
    var active: Boolean = false
}
