package io.eordie.multimodule.contracts.organization.models.structure

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.User
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.byIds
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationEmployeeFilterSummary(
    val totalCount: Long,
    val userIds: List<UuidStr>,
    val organizationIds: List<UuidStr>,
    val employeeIds: List<UuidStr>,
    val departmentIds: List<UuidStr>,
    val positionIds: List<UuidStr>
) {
    fun users(env: DataFetchingEnvironment) = env.byIds<User>(userIds)
    fun organizations(env: DataFetchingEnvironment) = env.byIds<Organization>(organizationIds)
    fun employees(env: DataFetchingEnvironment) = env.byIds<OrganizationEmployee>(employeeIds)
    fun departments(env: DataFetchingEnvironment) = env.byIds<OrganizationDepartment>(departmentIds)
    fun positions(env: DataFetchingEnvironment) = env.byIds<OrganizationPosition>(positionIds)
}
