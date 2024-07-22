package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.utils.associateFlattenById
import io.eordie.multimodule.common.utils.convert
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.organization.models.acl.BindingCriterion
import io.eordie.multimodule.contracts.organization.models.acl.GlobalCriterion
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartmentFilter
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployee
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeFilter
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPositionFilter
import io.eordie.multimodule.contracts.organization.services.OrganizationStructureQueries
import io.eordie.multimodule.contracts.utils.orDefault
import io.eordie.multimodule.organization.management.models.OrganizationDepartmentModel
import io.eordie.multimodule.organization.management.repository.OrganizationDepartmentFactory
import io.eordie.multimodule.organization.management.repository.OrganizationEmployeeFactory
import io.eordie.multimodule.organization.management.repository.OrganizationPositionFactory
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

@Singleton
class OrganizationStructureQueriesController(
    private val positions: OrganizationPositionFactory,
    private val employees: OrganizationEmployeeFactory,
    private val departments: OrganizationDepartmentFactory
) : OrganizationStructureQueries {

    override suspend fun positions(
        currentOrganization: CurrentOrganization,
        filter: OrganizationPositionFilter?
    ): List<OrganizationPosition> {
        val filterBy = filter.orDefault()
            .copy(organizationId = UUIDLiteralFilter(eq = currentOrganization.id))

        return positions.findAllByFilter(filterBy).map { it.convert() }.toList()
    }

    override suspend fun loadSubordinates(parentIds: List<UUID>): Map<UUID, List<OrganizationPosition>> {
        return positions.findByIdsWithSubordinates(parentIds).associateBy(
            { it.id },
            { parent -> parent.subordinates.map { it.convert() } }
        )
    }

    override suspend fun loadBindingsByDepartments(departmentIds: List<UUID>): Map<UUID, List<BindingCriterion>> {
        return departments.findByIds(departmentIds, departments.defaultFetcher)
            .associateFlattenById(departmentIds, OrganizationDepartmentModel::id) { department ->
                if (department.globalBinding != null) {
                    listOf(GlobalCriterion(requireNotNull(department.globalBinding)))
                } else {
                    (department.regionBindings + department.farmBindings).map { it.convert() }
                }
            }
    }

    override suspend fun departments(
        currentOrganization: CurrentOrganization,
        filter: OrganizationDepartmentFilter?,
        pageable: Pageable?
    ): Page<OrganizationDepartment> {
        val filterBy = filter.orDefault()
            .copy(organizationId = UUIDLiteralFilter(eq = currentOrganization.id))

        return departments.findByFilter(filterBy, pageable).convert()
    }

    override suspend fun employees(
        currentOrganization: CurrentOrganization,
        filter: OrganizationEmployeeFilter?,
        pageable: Pageable?
    ): Page<OrganizationEmployee> {
        val filterBy = filter.orDefault()
            .copy(organizationId = UUIDLiteralFilter(eq = currentOrganization.id))

        return employees.findByFilter(filterBy, pageable).convert()
    }
}
