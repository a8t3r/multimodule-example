package io.eordie.multimodule.graphql.gateway.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.identitymanagement.models.LocaleBinding
import io.eordie.multimodule.contracts.identitymanagement.models.OrganizationRoleBinding
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.graphql.gateway.security.RolesHolder
import io.micronaut.security.authentication.Authentication
import jakarta.inject.Singleton
import java.util.*

@Singleton
class AuthenticationDetailsBuilder(mapper: ObjectMapper) {

    init {
        Companion.mapper = mapper
    }

    companion object {

        private lateinit var mapper: ObjectMapper

        fun getRolesHolder(attributes: MutableMap<String, Any>): RolesHolder {
            return mapper.convertValue(attributes, RolesHolder::class.java)
        }

        fun of(authentication: Authentication?): AuthenticationDetails? {
            return if (authentication == null) null else {
                val attributes = authentication.attributes
                val holder = getRolesHolder(attributes)

                val localeLanguage = attributes["locale"] as? String
                AuthenticationDetails(
                    userId = UUID.fromString(authentication.name),
                    username = attributes["name"] as? String ?: attributes["preferred_username"] as String,
                    roles = Roles.supportedFrom(authentication.roles),
                    active = false,
                    emailVerified = attributes["email_verified"] as? Boolean ?: false,
                    locale = LocaleBinding("RU", localeLanguage ?: "en"),
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
}
