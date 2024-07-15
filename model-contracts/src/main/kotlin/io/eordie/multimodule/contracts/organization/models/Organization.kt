package io.eordie.multimodule.contracts.organization.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.basic.PermissionAware
import io.eordie.multimodule.contracts.basic.ShortDescription
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeFilter
import io.eordie.multimodule.contracts.organization.services.OrganizationQueries
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.getValueBy
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class Organization(
    override val id: UuidStr,
    override val name: String,
    val displayName: String?,
    override val permissions: List<Permission>
) : PermissionAware, ShortDescription {
    fun employedUsers(env: DataFetchingEnvironment, filter: OrganizationEmployeeFilter? = null) =
        env.getValueBy(OrganizationQueries::loadOrganizationEmployedUsers, id, filter)
}