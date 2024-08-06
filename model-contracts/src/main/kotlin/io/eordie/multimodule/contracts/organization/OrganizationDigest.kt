package io.eordie.multimodule.contracts.organization

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class OrganizationDigest(
    @GraphQLIgnore
    val organizationId: UuidStr,
    val membersCount: Long,
    val domainsCount: Long,
    val departmentsCount: Long,
    val positionsCount: Long,
    val employeesCount: Long
)
