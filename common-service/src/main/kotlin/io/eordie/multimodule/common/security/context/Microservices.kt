package io.eordie.multimodule.common.security.context

import io.eordie.multimodule.common.repository.ResourceAcl
import io.eordie.multimodule.contracts.organization.models.acl.EmployeeAcl
import io.eordie.multimodule.contracts.organization.services.EmployeeAclQueries
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@Singleton
class Microservices @Inject constructor(private val api: EmployeeAclQueries) {

    suspend fun loadAclElement(): AclContextElement {
        val element = coroutineContext.getOrCreateAclElement()
        if (!element.isInitialized()) {
            element.initialize(loadAcl(coroutineContext))
        }
        return element
    }

    private fun loadAcl(context: CoroutineContext, userId: UUID, organizationId: UUID): List<EmployeeAcl> {
        return api.loadEmployeeAcl(context, userId, organizationId)
    }

    fun buildAcl(context: CoroutineContext, requireEmployeeAcl: Boolean = true): ResourceAcl {
        val employeeAcl = if (requireEmployeeAcl) loadAcl(context) else emptyList()
        return ResourceAcl(context.getAuthenticationContext(), employeeAcl)
    }

    private fun loadAcl(context: CoroutineContext): List<EmployeeAcl> {
        val auth = context.getAuthenticationContext()
        val organizationId = auth.currentOrganizationId
        return if (organizationId == null) emptyList() else {
            loadAcl(context, auth.userId, organizationId)
        }
    }
}
