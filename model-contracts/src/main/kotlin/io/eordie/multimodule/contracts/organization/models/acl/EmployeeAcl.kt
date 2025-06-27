package io.eordie.multimodule.contracts.organization.models.acl

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class EmployeeAcl(
    val farmId: UuidStr,
    val farmOwnerOrganizationId: UuidStr,
    val fieldIds: List<UuidStr>? = null,
    @property:GraphQLIgnore val roleIds: List<Int>? = null
) {
    fun roles() = Roles.fromIds(roleIds)
}
