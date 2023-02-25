package io.eordie.multimodule.example.contracts.identitymanagement.models

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.expediagroup.graphql.generator.annotations.GraphQLName
import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.organization.models.Organization
import io.eordie.multimodule.example.contracts.organization.models.User
import io.eordie.multimodule.example.contracts.organization.services.UserQueries
import io.eordie.multimodule.example.contracts.utils.Roles
import io.eordie.multimodule.example.contracts.utils.UuidStr
import io.eordie.multimodule.example.contracts.utils.byId
import io.eordie.multimodule.example.contracts.utils.getValueBy
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

@Introspected
@Serializable
data class OrganizationRoleBinding(
    val organizationId: UuidStr,
    val organizationName: String,
    @GraphQLIgnore val roles: List<Roles>
) {

    @GraphQLIgnore
    fun hasRole(role: Roles): Boolean = roles.contains(role)

    @GraphQLName("roles")
    fun humanRoles(role: String? = null): List<String> = roles
        .map { it.humanName() }
        .filter { role == null || role == it }
}

@Introspected
@Serializable
data class AuthenticationDetails(
    val userId: UuidStr,
    val username: String,
    @GraphQLIgnore val roles: List<Roles>,
    val active: Boolean,
    val emailVerified: Boolean,
    val currentOrganizationId: UuidStr? = null,
    val organizationRoles: List<OrganizationRoleBinding>? = null
) {
    val organizationIds: Set<UuidStr> = organizationRoles.orEmpty().map { it.organizationId }.toSet()

    @GraphQLIgnore
    fun organizationsWithRole(role: Roles): Set<UuidStr> {
        return organizationRoles.orEmpty()
            .filter { it.hasRole(role) }
            .map { it.organizationId }
            .toSet()
    }

    @GraphQLIgnore fun hasRole(role: Roles): Boolean = roles.contains(role)

    @GraphQLIgnore fun hasAnyRole(vararg required: Roles): Boolean = required.any { hasRole(it) }

    @GraphQLIgnore fun hasAllRoles(vararg required: Roles): Boolean = required.all { hasRole(it) }

    fun user(env: DataFetchingEnvironment): CompletableFuture<User> {
        return env.getValueBy(UserQueries::loadUserByIds, userId)
    }

    @GraphQLName("roles")
    fun humanRoles(role: String? = null): List<String> = roles
        .map { it.humanName() }
        .filter { role == null || role == it }

    fun currentOrganization(env: DataFetchingEnvironment): CompletableFuture<Organization?> {
        return if (currentOrganizationId == null) CompletableFuture.completedFuture(null) else {
            env.byId<Organization?>(currentOrganizationId)
        }
    }
}
