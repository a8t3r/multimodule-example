package io.eordie.multimodule.contracts.organization.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.byIds
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationSummary(
    val totalCount: Long,
    val usersCount: Long,
    val organizationIds: List<UuidStr>,
    val userIds: List<UuidStr>
) {
    fun members(env: DataFetchingEnvironment) = env.byIds<User>(userIds)
}
