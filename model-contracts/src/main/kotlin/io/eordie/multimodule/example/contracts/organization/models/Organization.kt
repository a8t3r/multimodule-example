package io.eordie.multimodule.example.contracts.organization.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.organization.services.OrganizationQueries
import io.eordie.multimodule.example.contracts.utils.UuidStr
import io.eordie.multimodule.example.contracts.utils.getValueBy
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

@Introspected
@Serializable
data class Organization(
    val id: UuidStr,
    val name: String,
    val displayName: String?
) {
    fun members(env: DataFetchingEnvironment): CompletableFuture<List<User>> {
        return env.getValueBy(OrganizationQueries::loadOrganizationMembers, id)
    }
}
