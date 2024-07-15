package io.eordie.multimodule.contracts.identitymanagement.models

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.expediagroup.graphql.generator.annotations.GraphQLName
import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.basic.ShortDescription
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.User
import io.eordie.multimodule.contracts.organization.services.UserQueries
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.byId
import io.eordie.multimodule.contracts.utils.byIds
import io.eordie.multimodule.contracts.utils.getValueBy
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
    val locale: LocaleBinding,
    val currentOrganizationId: UuidStr? = null,
    val organizationRoles: List<OrganizationRoleBinding>? = null
) {

    fun organizationIds(): List<UuidStr> = organizationRoles.orEmpty().map { it.organizationId }

    fun organizations(env: DataFetchingEnvironment): CompletableFuture<List<ShortDescription>> =
        env.byIds<Organization?>(organizationIds())
            .thenApply { it.filterNotNull() }

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