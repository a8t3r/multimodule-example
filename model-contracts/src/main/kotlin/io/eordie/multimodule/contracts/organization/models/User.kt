package io.eordie.multimodule.contracts.organization.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.organization.services.UserQueries
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.getValueBy
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

@Introspected
@Serializable
data class User(
    val id: UuidStr,
    val firstName: String,
    val lastName: String,
    val email: String,
    val emailVerified: Boolean,
    val enabled: Boolean
) {
    fun roles(env: DataFetchingEnvironment, role: String? = null): CompletableFuture<List<String>> {
        return env.getValueBy(UserQueries::loadRolesByUserIds, id, role)
    }
}
