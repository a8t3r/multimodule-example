package io.eordie.multimodule.contracts.organization.models.structure

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.User
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.byId
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationEmployee(
    val userId: UuidStr,
    val organizationId: UuidStr,
    val departmentId: UuidStr,
    val positionId: UuidStr
) {
    fun user(env: DataFetchingEnvironment) = env.byId<User>(userId)
    fun organization(env: DataFetchingEnvironment) = env.byId<Organization>(organizationId)
    fun position(env: DataFetchingEnvironment) = env.byId<OrganizationPosition>(positionId)
    fun department(env: DataFetchingEnvironment) = env.byId<OrganizationDepartment>(departmentId)
}
