package io.eordie.multimodule.example.contracts.organization.models.structure

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.Auditable
import io.eordie.multimodule.example.contracts.organization.models.Organization
import io.eordie.multimodule.example.contracts.organization.services.OrganizationStructureQueries
import io.eordie.multimodule.example.contracts.utils.OffsetDateTimeStr
import io.eordie.multimodule.example.contracts.utils.UuidStr
import io.eordie.multimodule.example.contracts.utils.byId
import io.eordie.multimodule.example.contracts.utils.getValueBy
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

@Introspected
@Serializable
data class OrganizationPosition(
    val id: UuidStr,
    val name: String,
    val organizationId: UuidStr,
    val roles: List<String>,
    val parentId: UuidStr?,
    override val deleted: Boolean,
    override val createdAt: OffsetDateTimeStr,
    override val updatedAt: OffsetDateTimeStr
) : Auditable {
    fun organization(env: DataFetchingEnvironment): CompletableFuture<Organization> = env.byId(organizationId)

    fun parent(env: DataFetchingEnvironment): CompletableFuture<OrganizationPosition?> {
        return if (parentId == null) CompletableFuture.completedFuture(null) else {
            env.byId(parentId)
        }
    }

    fun subordinates(env: DataFetchingEnvironment): CompletableFuture<List<OrganizationPosition>> {
        return env.getValueBy(OrganizationStructureQueries::loadSubordinates, id)
    }
}
