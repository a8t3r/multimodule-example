package io.eordie.multimodule.example.contracts.organization.models.structure

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.organization.models.Organization
import io.eordie.multimodule.example.contracts.utils.UuidStr
import io.eordie.multimodule.example.contracts.utils.byId
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

@Introspected
@Serializable
data class OrganizationDepartment(
    val id: UuidStr,
    val name: String,
    val organizationId: UuidStr
) {
    fun organization(env: DataFetchingEnvironment): CompletableFuture<Organization> = env.byId(organizationId)
}
