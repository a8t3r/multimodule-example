package io.eordie.multimodule.example.apitests

import io.eordie.multimodule.example.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.example.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.example.contracts.identitymanagement.models.OrganizationRoleBinding
import io.eordie.multimodule.example.contracts.utils.Roles
import io.eordie.multimodule.example.rsocket.context.AuthenticationContextElement
import io.micronaut.security.authentication.ServerAuthentication
import java.util.*

object AuthUtils {
    fun authWith(currentOrganization: CurrentOrganization, vararg roles: Roles) = AuthenticationContextElement(
        AuthenticationDetails(
            UUID.randomUUID(),
            "api-tests",
            roles = roles.toList(),
            active = true,
            emailVerified = true,
            currentOrganizationId = currentOrganization.id,
            organizationRoles = listOf(
                OrganizationRoleBinding(
                    currentOrganization.id,
                    "default",
                    roles.toList()
                )
            )
        )
    )

    fun AuthenticationDetails.getAuthentication(): ServerAuthentication {
        val (userId, username, roles) = this
        return ServerAuthentication(
            userId.toString(),
            roles.map { it.humanName() },
            mapOf("preferred_username" to username)
        )
    }
}
