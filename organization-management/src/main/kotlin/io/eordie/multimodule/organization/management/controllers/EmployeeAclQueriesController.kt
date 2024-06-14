package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.rsocket.context.Microservices
import io.eordie.multimodule.contracts.organization.models.acl.EmployeeAcl
import io.eordie.multimodule.contracts.organization.services.EmployeeAclQueries
import io.eordie.multimodule.organization.management.repository.EmployeeAclRepository
import jakarta.inject.Provider
import jakarta.inject.Singleton
import java.util.*
import kotlin.coroutines.CoroutineContext

@Singleton
class EmployeeAclQueriesController(
    private val microservices: Provider<Microservices>,
    private val employeeAclRepository: EmployeeAclRepository
) : EmployeeAclQueries {

    override suspend fun currentEmployeeAcl(): List<EmployeeAcl> = microservices.get().loadAclElement().resource

    override fun loadEmployeeAcl(context: CoroutineContext, userId: UUID, organizationId: UUID): List<EmployeeAcl> {
        return employeeAclRepository.findAccessible(organizationId, userId)
    }
}
