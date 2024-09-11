package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.validation.error
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.organization.models.acl.BindingCriterion
import io.eordie.multimodule.contracts.organization.models.acl.ByFarmCriterion
import io.eordie.multimodule.contracts.organization.models.acl.ByRegionCriterion
import io.eordie.multimodule.contracts.organization.services.OrganizationDepartmentMutations
import io.eordie.multimodule.contracts.organization.services.OrganizationStructureQueries
import io.eordie.multimodule.organization.management.models.acl.ByFarmCriterionModel
import io.eordie.multimodule.organization.management.models.acl.ByRegionCriterionModel
import io.eordie.multimodule.organization.management.repository.OrganizationDepartmentFactory
import io.eordie.multimodule.organization.management.validation.DepartmentNotBelongsToOrganization
import jakarta.inject.Singleton
import java.util.*

@Singleton
class OrganizationDepartmentMutationsController(
    private val queries: OrganizationStructureQueries,
    private val departments: OrganizationDepartmentFactory
) : OrganizationDepartmentMutations {

    private fun ensureEquality(
        currentOrganization: CurrentOrganization,
        organizationId: UUID
    ) {
        if (organizationId != currentOrganization.id) {
            DepartmentNotBelongsToOrganization.error()
        }
    }

    private suspend fun departmentBindings(departmentId: UUID): List<BindingCriterion> =
        queries.loadBindingsByDepartments(listOf(departmentId)).getValue(departmentId)

    override suspend fun modifyGlobalBinding(
        currentOrganization: CurrentOrganization,
        departmentId: UUID,
        includeAll: Boolean
    ): List<BindingCriterion> {
        val department = departments.save(
            departmentId,
            departments.defaultFetcher
        ) { _, value ->
            ensureEquality(currentOrganization, value.organizationId)

            value.globalBinding = includeAll
            value.regionBindings = emptyList()
            value.farmBindings = emptyList()
        }

        return departmentBindings(department.id)
    }

    override suspend fun modifyByRegionCriterion(
        currentOrganization: CurrentOrganization,
        departmentId: UUID,
        binding: ByRegionCriterion,
        plus: Boolean
    ): List<BindingCriterion> {
        val model = ByRegionCriterionModel {
            this.departmentId = departmentId
            this.regionId = binding.regionId
        }

        val department = departments.save(
            departmentId,
            departments.defaultFetcher
        ) { _, value ->
            ensureEquality(currentOrganization, value.organizationId)

            value.globalBinding = null
            value.regionBindings = if (plus) {
                val contains = value.regionBindings.any { it.regionId == model.regionId }
                if (contains) value.regionBindings else {
                    value.regionBindings + model
                }
            } else {
                value.regionBindings.filter { it.regionId != model.regionId }
            }
        }

        return departmentBindings(department.id)
    }

    override suspend fun modifyByFarmCriterion(
        currentOrganization: CurrentOrganization,
        departmentId: UUID,
        binding: ByFarmCriterion,
        plus: Boolean
    ): List<BindingCriterion> {
        val model = ByFarmCriterionModel {
            this.departmentId = departmentId
            this.farmId = binding.farmId
            this.fieldIds = binding.fieldIds
        }

        val department = departments.save(
            departmentId,
            departments.defaultFetcher
        ) { _, value ->
            ensureEquality(currentOrganization, value.organizationId)

            value.globalBinding = null
            value.farmBindings = if (plus) {
                val index = value.farmBindings.indexOfFirst { it.farmId == model.farmId }
                if (index < 0) value.farmBindings + model else {
                    val mutable = ArrayList(value.farmBindings)
                    mutable[index] = model
                    mutable
                }
            } else {
                value.farmBindings.filter { it.farmId != model.farmId }
            }
        }

        return departmentBindings(department.id)
    }
}
