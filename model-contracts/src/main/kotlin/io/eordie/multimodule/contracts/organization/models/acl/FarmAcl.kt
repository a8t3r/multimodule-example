package io.eordie.multimodule.contracts.organization.models.acl

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import io.eordie.multimodule.contracts.basic.BasePermission
import io.eordie.multimodule.contracts.basic.PermissionAware
import io.eordie.multimodule.contracts.basic.filters.Direction
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.*

@Introspected
@Serializable
data class FarmAcl(
    val id: UuidStr,
    val organizationId: UuidStr,
    val farmOwnerOrganizationId: UuidStr,
    val roles: List<Roles>,
    val farmId: UuidStr,
    val fieldIds: List<UuidStr>? = null,
    override val permissions: List<BasePermission>
) : PermissionAware<BasePermission> {

    @GraphQLIgnore
    fun direction(relatedOrganizationId: UUID): Direction? = when (relatedOrganizationId) {
        organizationId -> Direction.INCOME
        farmOwnerOrganizationId -> Direction.OUTCOME
        else -> null
    }
}
