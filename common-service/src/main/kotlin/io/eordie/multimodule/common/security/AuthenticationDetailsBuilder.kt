package io.eordie.multimodule.common.security

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.identitymanagement.models.LocaleBinding
import io.eordie.multimodule.contracts.identitymanagement.models.OrganizationRoleBinding
import io.eordie.multimodule.contracts.utils.Roles
import io.micronaut.security.authentication.Authentication
import java.util.*

class AuthenticationDetailsBuilder {

    class RolesHolder {
        @JsonProperty("resource_access")
        var resourceAccess: ResourceAccess? = null
        var activeOrganization: ActiveOrganization? = null
        var organizationRoles: Map<String, OrganizationRole>? = null
        var organizationAttributes: Map<String, OrganizationAttribute>? = null

        fun roles(): List<Roles> {
            return Roles.supportedFrom(
                resourceAccess?.masterRealm?.roles.orEmpty() + activeOrganization?.role.orEmpty()
            )
        }
    }

    data class ActiveOrganization(
        val role: List<String>?,
        val id: String?
    )

    data class ResourceAccess(
        @JsonProperty("master-realm")
        val masterRealm: ResourceRole
    )

    data class ResourceRole(
        val roles: List<String>
    )

    data class OrganizationAttribute(
        val name: String,
        val attributes: Map<String, List<String>>
    )

    data class OrganizationRole(
        val name: String,
        val roles: List<String> = emptyList()
    )

    companion object {

        private val mapper = ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .registerModule(KotlinModule.Builder().build())

        fun getRolesHolder(attributes: MutableMap<String, Any>): RolesHolder {
            return mapper.convertValue(attributes, RolesHolder::class.java)
        }

        fun of(authentication: Authentication): AuthenticationDetails = of(
            UUID.fromString(authentication.name),
            authentication.attributes
        )

        private fun of(
            userId: UUID,
            attributes: MutableMap<String, Any>
        ): AuthenticationDetails {
            val holder = getRolesHolder(attributes)

            return AuthenticationDetails(
                userId = userId,
                username = attributes["name"] as? String ?: attributes["preferred_username"] as String,
                roles = holder.roles(),
                active = false,
                email = attributes.getValue("email") as String,
                emailVerified = attributes["email_verified"] as? Boolean ?: false,
                locale = LocaleBinding("RU", attributes["locale"] as? String ?: "en"),
                currentOrganizationId = holder.activeOrganization?.id?.let { UUID.fromString(it) },
                organizationRoles = holder.organizationRoles?.entries
                    ?.map {
                        OrganizationRoleBinding(
                            UUID.fromString(it.key),
                            it.value.name,
                            Roles.supportedFrom(it.value.roles)
                        )
                    }
            )
        }
    }
}
