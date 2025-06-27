package io.eordie.multimodule.common.security.context

import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.organization.models.acl.EmployeeAcl
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.contracts.organization.services.EmployeeAclQueries
import jakarta.inject.Inject
import jakarta.inject.Provider
import jakarta.inject.Singleton
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Singleton
class Microservices @Inject constructor(
    private val api: Provider<EmployeeAclQueries>
) {

    fun loadAclElement(coroutineContext: CoroutineContext): AclContextElement {
        val element = coroutineContext.getOrCreateAclElement()
        if (!element.isInitialized()) {
            element.initialize(loadAcl(coroutineContext.getAuthenticationContext()))
        }
        return element
    }

    private fun loadAcl(context: CoroutineContext, userId: UUID, organizationId: UUID): List<EmployeeAcl> {
        return api.get().loadEmployeeAcl(context, userId, organizationId)
    }

    fun buildAcl(context: CoroutineContext, requireEmployeeAcl: Boolean = true): ResourceAcl {
        val auth = context.getAuthenticationContext()
        val employeeAcl = if (requireEmployeeAcl) loadAclElement(context).resource else emptyList()
        return ResourceAcl(auth, employeeAcl)
    }

    private fun loadAcl(auth: AuthenticationDetails): List<EmployeeAcl> {
        val organizationId = auth.currentOrganizationId
        return if (organizationId == null) emptyList() else {
            loadAcl(EmptyCoroutineContext, auth.userId, organizationId)
        }
    }
}
