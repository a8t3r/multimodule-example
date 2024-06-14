package io.eordie.multimodule.api.tests

import io.eordie.multimodule.common.rsocket.context.AuthenticationContextElement
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.identitymanagement.models.LocaleBinding
import io.eordie.multimodule.contracts.identitymanagement.models.OrganizationRoleBinding
import io.eordie.multimodule.contracts.utils.Roles
import java.util.*

object AuthUtils {

    fun authWith(builder: AuthenticationDetails.() -> AuthenticationDetails): AuthenticationContextElement {
        val initial = AuthenticationDetails(
            UUID.randomUUID(),
            "api-tests",
            roles = emptyList(),
            active = true,
            emailVerified = true,
            currentOrganizationId = null,
            locale = LocaleBinding.default(),
            organizationRoles = emptyList()
        )
        return AuthenticationContextElement(builder.invoke(initial))
    }

    fun authWith(currentOrganization: CurrentOrganization, vararg roles: Roles) = authWith {
        this.copy(
            currentOrganizationId = currentOrganization.id,
            roles = roles.toList(),
            organizationRoles = listOf(
                OrganizationRoleBinding(
                    currentOrganization.id,
                    "default",
                    roles.toList()
                )
            )
        )
    }
}
