package io.eordie.multimodule.common.repository

import com.google.common.collect.Iterables
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.organization.models.acl.EmployeeAcl
import io.eordie.multimodule.contracts.utils.Roles
import java.util.*

class ResourceAcl(
    val auth: AuthenticationDetails,
    val entries: List<EmployeeAcl>
) {

    val allOrganizationIds: Set<UUID> = Iterables.concat(
        auth.organizationIds(),
        entries.map { it.farmOwnerOrganizationId }
    ).toSet()

    fun organizationsWithRole(roles: Set<Roles>): Set<UUID> {
        return auth.organizationRoles.orEmpty()
            .filter { it.roles.containsAll(roles) }
            .map { it.organizationId }
            .toSet()
    }

    fun hasOrganizationRole(role: Roles) = auth.roles.contains(role)

    fun hasAnyOrganizationRole(vararg required: Roles) = hasAnyOrganizationRole(required.toSet())
    fun hasAnyOrganizationRole(required: Collection<Roles>) = required.any { hasOrganizationRole(it) }

    fun hasAllOrganizationRoles(vararg required: Roles) = hasAllOrganizationRoles(required.toSet())
    fun hasAllOrganizationRoles(required: Collection<Roles>) = required.all { hasOrganizationRole(it) }
}
