package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.security.context.Microservices
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.identitymanagement.models.LocaleBinding
import io.eordie.multimodule.contracts.identitymanagement.models.OrganizationRoleBinding
import io.eordie.multimodule.contracts.organization.models.acl.DepartmentBindingFilter
import io.eordie.multimodule.contracts.organization.models.acl.EmployeeAcl
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.contracts.organization.services.EmployeeAclQueries
import io.eordie.multimodule.organization.management.models.OrganizationEmployeeModel
import io.eordie.multimodule.organization.management.repository.OrganizationDepartmentFactory
import io.eordie.multimodule.organization.management.repository.OrganizationEmployeeFactory
import jakarta.inject.Provider
import jakarta.inject.Singleton
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@Singleton
class EmployeeAclQueriesController(
    private val microservices: Provider<Microservices>,
    private val employees: OrganizationEmployeeFactory,
    private val departments: OrganizationDepartmentFactory
) : EmployeeAclQueries {

    override suspend fun activeEmployeeAcl(): List<EmployeeAcl> =
        microservices.get().loadAclElement(coroutineContext).resource

    override suspend fun internalActiveResourceAcl(userId: UUID, organizationId: UUID): ResourceAcl? {
        return loadResourceAcl(listOf(userId), organizationId).firstOrNull()
    }

    override fun loadEmployeeAcl(context: CoroutineContext, userId: UUID, organizationId: UUID): List<EmployeeAcl> {
        val employees = employees.getEmployeesByOrganization(listOf(userId), organizationId)
        return if (employees.isEmpty()) emptyList() else departments.findBindingsByFilter(
            DepartmentBindingFilter(
                organizationId = UUIDLiteralFilter(eq = organizationId),
                departmentId = UUIDLiteralFilter(of = employees.mapNotNull { it.departmentId })
            )
        ).map { binding ->
            EmployeeAcl(binding.farmId, binding.farmOwnerOrganizationId, binding.fieldIds, binding.roleIds)
        }
    }

    override suspend fun loadResourceAcl(userIds: List<UUID>, organizationId: UUID): List<ResourceAcl> {
        val employees = employees.getEmployeesByOrganization(userIds, organizationId)
        val employeeIndex = employees.groupBy { it.userId }
        val bindingIndex = if (employees.isEmpty()) emptyMap() else {
            departments.findBindingsByFilter(
                DepartmentBindingFilter(
                    organizationId = UUIDLiteralFilter(eq = organizationId),
                    departmentId = UUIDLiteralFilter(of = employees.mapNotNull { it.departmentId })
                )
            ).groupBy { it.departmentId }
        }

        return employeeIndex.entries.map { (userId, employees) ->
            val details = buildAuthentication(userId, employees)
            val employeeAcl = employees.mapNotNull { bindingIndex[it.departmentId] }
                .flatten()
                .map { binding ->
                    EmployeeAcl(binding.farmId, binding.farmOwnerOrganizationId, binding.fieldIds, binding.roleIds)
                }
            ResourceAcl(details, employeeAcl)
        }
    }

    private fun buildAuthentication(
        userId: UUID,
        employees: List<OrganizationEmployeeModel>
    ): AuthenticationDetails {
        val (user, organization) = with(employees.first()) { user to organization }
        val roles = employees.mapNotNull { it.position?.roles }.flatten().distinct()

        return AuthenticationDetails(
            userId,
            roles,
            user.email,
            user.emailVerified,
            LocaleBinding.default(),
            organization.id,
            listOf(
                OrganizationRoleBinding(
                    organization.id,
                    organization.name,
                    roles
                )
            )
        )
    }
}
