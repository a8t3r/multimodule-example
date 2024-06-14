package io.eordie.multimodule.contracts.organization.models.structure

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.acl.BindingCriterion
import io.eordie.multimodule.contracts.organization.services.OrganizationStructureQueries
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.byId
import io.eordie.multimodule.contracts.utils.getValueBy
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

    fun bindings(env: DataFetchingEnvironment): CompletableFuture<List<BindingCriterion>> =
        env.getValueBy(OrganizationStructureQueries::loadBindingsByDepartments, id)

    fun organization(env: DataFetchingEnvironment): CompletableFuture<Organization> = env.byId(organizationId)
}
