package io.eordie.multimodule.example.contracts.organization.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.utils.UuidStr
import io.eordie.multimodule.example.contracts.utils.byId
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationDomain(
    val id: UuidStr,
    val domain: String,
    val verified: Boolean,
    val organizationId: UuidStr
) {
    fun organization(env: DataFetchingEnvironment) = env.byId<Organization>(organizationId)
}
