package io.eordie.multimodule.example.gateway.security

import com.fasterxml.jackson.annotation.JsonProperty
import io.eordie.multimodule.example.contracts.utils.Roles

class RolesHolder {
    @JsonProperty("resource_access")
    var resourceAccess: ResourceAccess? = null
    var activeOrganization: ActiveOrganization? = null
    var organizationRoles: Map<String, OrganizationRole>? = null
    var organizationAttributes: Map<String, OrganizationAttribute>? = null

    fun roles(): Collection<Roles> {
        return Roles.supportedFrom(
            resourceAccess?.masterRealm?.roles.orEmpty() + activeOrganization?.role.orEmpty()
        )
    }
}

data class ActiveOrganization(
    val role: List<String>,
    val id: String
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
    val roles: List<String>
)
