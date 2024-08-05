package io.eordie.multimodule.graphql.gateway.security

import io.eordie.multimodule.common.security.AuthenticationDetailsBuilder
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
