package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.validation.Cycle
import io.eordie.multimodule.common.validation.error
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartmentInput
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployee
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeInput
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPositionInput
import io.eordie.multimodule.contracts.organization.services.OrganizationStructureMutations
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.OrganizationDepartmentModelDraft
import io.eordie.multimodule.organization.management.models.OrganizationEmployeeModelDraft
import io.eordie.multimodule.organization.management.models.OrganizationPositionModelDraft
import io.eordie.multimodule.organization.management.models.departmentId
import io.eordie.multimodule.organization.management.models.organizationId
import io.eordie.multimodule.organization.management.models.parentId
import io.eordie.multimodule.organization.management.models.userId
import io.eordie.multimodule.organization.management.repository.OrganizationEmployeeFactory
import io.eordie.multimodule.organization.management.repository.OrganizationMemberFactory
import io.eordie.multimodule.organization.management.repository.OrganizationPositionFactory
import io.eordie.multimodule.organization.management.repository.OrganizationPositionsRepository
import io.eordie.multimodule.organization.management.validation.MissingMembership
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import java.util.*

@Singleton
class OrganizationStructureMutationsController(
    private val members: OrganizationMemberFactory,
    private val employees: OrganizationEmployeeFactory,
    private val positions: OrganizationPositionFactory,
    private val departments: io.eordie.multimodule.organization.management.repository.OrganizationDepartmentFactory,
    private val positionsRepository: OrganizationPositionsRepository
) : OrganizationStructureMutations {

    override suspend fun position(
        currentOrganization: CurrentOrganization,
        position: OrganizationPositionInput
    ): OrganizationPosition {
        return positions.save<OrganizationPositionModelDraft>(position.id) { _, value ->
            value.parentId = position.parentId
            value.roleIds = Roles.idsFromNames(position.roles)
            value.name = position.name
        }.convert()
    }

    override suspend fun deletePosition(positionId: UUID): Boolean {
        return positions.deletePosition(positionId)
    }

    override suspend fun changePositionsParent(previousParentId: UUID, newParentId: UUID?): Boolean {
        if (newParentId != null) {
            val parentIds = positionsRepository.getParentIds(newParentId)
            positions.findIdsBySpecification {
                where(table.parentId eq previousParentId)
            }.collect {
                if (parentIds.contains(it)) Cycle.error()
            }
        }
        return positions.changeParent(previousParentId, newParentId)
    }

    override suspend fun deleteDepartment(departmentId: UUID): Boolean = departments.deleteById(departmentId)

    override suspend fun department(
        currentOrganization: CurrentOrganization,
        input: OrganizationDepartmentInput
    ): OrganizationDepartment {
        return departments.save<OrganizationDepartmentModelDraft>(input.id) { _, value ->
            value.name = input.name
        }.convert()
    }

    override suspend fun employee(
        currentOrganization: CurrentOrganization,
        employeeInput: OrganizationEmployeeInput
    ): OrganizationEmployee {
        val membership = members.findOneBySpecification {
            where(
                table.userId eq employeeInput.userId,
                table.organizationId eq currentOrganization.id
            )
        } ?: MissingMembership.error()

        return employees.save<OrganizationEmployeeModelDraft>(null) { _, value ->
            value.memberId = membership.id
            value.userId = employeeInput.userId
            value.departmentId = employeeInput.departmentId
            value.positionId = employeeInput.positionId
        }.convert()
    }

    override suspend fun deleteEmployee(
        currentOrganization: CurrentOrganization,
        departmentId: UUID?,
        userId: UUID
    ): Boolean {
        val employee = employees.findOneBySpecification {
            where(
                table.userId eq userId,
                table.organizationId eq currentOrganization.id,
                table.departmentId eq departmentId
            )
        }
        return employee?.id?.let { employees.deleteById(it) } ?: false
    }
}
