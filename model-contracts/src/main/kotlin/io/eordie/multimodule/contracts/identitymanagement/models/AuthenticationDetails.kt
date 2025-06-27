package io.eordie.multimodule.contracts.identitymanagement.models

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.basic.Named
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.User
import io.eordie.multimodule.contracts.organization.services.UserQueries
import io.eordie.multimodule.contracts.utils.RoleSet
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.asList
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
    @property:GraphQLIgnore val roleSet: RoleSet
) {
    val roles: List<Roles> get() = roleSet.asList()
}

@Introspected
@Serializable
data class AuthenticationDetails(
    val userId: UuidStr,
    @property:GraphQLIgnore val roleSet: RoleSet,
    val email: String,
    val emailVerified: Boolean,
    val locale: LocaleBinding,
    val currentOrganizationId: UuidStr? = null,
    val organizationRoles: List<OrganizationRoleBinding>? = null
) {
    val roles: List<Roles> get() = roleSet.asList()

    fun organizationIds(): Set<UuidStr> = organizationRoles.orEmpty().map { it.organizationId }.toSet()

    fun organizations(env: DataFetchingEnvironment): CompletableFuture<List<Named>> =
        env.byIds<Organization?>(organizationIds().toList())
            .thenApply { it.filterNotNull() }

    fun user(env: DataFetchingEnvironment): CompletableFuture<User> {
        return env.getValueBy(UserQueries::loadUserByIds, userId)
    }

    fun currentOrganization(env: DataFetchingEnvironment) = env.byId<Organization?>(currentOrganizationId)
}
