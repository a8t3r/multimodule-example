package io.eordie.multimodule.example.gateway.security

import io.eordie.multimodule.example.gateway.graphql.AuthenticationDetailsBuilder
import io.micronaut.context.annotation.Primary
import io.micronaut.security.token.RolesFinder
import jakarta.inject.Singleton

@Primary
@Singleton
class KeycloakRolesFinder : RolesFinder {

    override fun resolveRoles(attributes: MutableMap<String, Any>): MutableList<String> {
        val holder = AuthenticationDetailsBuilder.getRolesHolder(attributes)
        return holder.roles().map { it.humanName() }.toMutableList()
    }
}
