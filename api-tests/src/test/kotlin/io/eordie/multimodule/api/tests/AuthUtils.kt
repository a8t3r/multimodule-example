package io.eordie.multimodule.api.tests

import io.eordie.multimodule.common.security.context.AuthenticationContextElement
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.identitymanagement.models.LocaleBinding
import io.eordie.multimodule.contracts.identitymanagement.models.OrganizationRoleBinding
import io.eordie.multimodule.contracts.utils.RoleSet
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.contracts.utils.asRoleSet
import java.util.*

object AuthUtils {

    fun authWith(builder: AuthenticationDetails.() -> AuthenticationDetails): AuthenticationContextElement {
        val initial = AuthenticationDetails(
            UUID.randomUUID(),
            roleSet = RoleSet.noneOf(Roles::class.java),
            email = "test",
            emailVerified = true,
            currentOrganizationId = null,
            locale = LocaleBinding.default(),
            organizationRoles = emptyList()
        )
        return AuthenticationContextElement(builder.invoke(initial))
    }

    fun authWith(
        currentOrganization: CurrentOrganization,
        builder: AuthenticationDetails.() -> AuthenticationDetails
    ) = authWith {
        builder.invoke(this).copy(
            currentOrganizationId = currentOrganization.id,
            organizationRoles = listOf(
                OrganizationRoleBinding(
                    currentOrganization.id,
                    "default",
                    roles.toList().asRoleSet()
                )
            )
        )
    }

    fun authWith(currentOrganization: CurrentOrganization, vararg roles: Roles) =
        authWith(currentOrganization, UUID.randomUUID(), *roles)

    fun authWith(currentOrganization: CurrentOrganization, userId: UUID, vararg roles: Roles) =
        authWith(currentOrganization) { this.copy(roleSet = roles.asList().asRoleSet(), userId = userId) }
}
