package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.rsocket.context.getAuthenticationContext
import io.eordie.multimodule.contracts.basic.exception.ValidationException
import io.eordie.multimodule.contracts.organization.models.acl.BindingCriterion
import io.eordie.multimodule.contracts.organization.models.acl.ByFarmCriterion
import io.eordie.multimodule.contracts.organization.models.acl.ByRegionCriterion
import io.eordie.multimodule.contracts.organization.services.OrganizationDepartmentMutations
import io.eordie.multimodule.contracts.organization.services.OrganizationStructureQueries
import io.eordie.multimodule.organization.management.models.OrganizationDepartmentModel
import io.eordie.multimodule.organization.management.models.OrganizationDepartmentModelDraft
import io.eordie.multimodule.organization.management.models.acl.ByFarmCriterionModel
import io.eordie.multimodule.organization.management.models.acl.ByRegionCriterionModel
import io.eordie.multimodule.organization.management.models.acl.by
import jakarta.inject.Singleton
import org.babyfish.jimmer.kt.new
import java.util.*
import kotlin.coroutines.coroutineContext

@Singleton
class OrganizationDepartmentMutationsController(
    private val queries: OrganizationStructureQueries,
    private val departments: io.eordie.multimodule.organization.management.repository.OrganizationDepartmentFactory
) : OrganizationDepartmentMutations {

    private suspend fun getDepartmentById(departmentId: UUID): OrganizationDepartmentModel {
        return departments.getById(departmentId)
            .takeIf { it.organizationId == coroutineContext.getAuthenticationContext().currentOrganizationId }
            ?: throw ValidationException("department must belong to the organization")
    }

    private suspend fun departmentBindings(departmentId: UUID): List<BindingCriterion> =
        queries.loadBindingsByDepartments(listOf(departmentId)).getValue(departmentId)

    override suspend fun modifyGlobalBinding(
        departmentId: UUID,
        includeAll: Boolean
    ): List<BindingCriterion> {
        val department = getDepartmentById(departmentId)
        departments.save<OrganizationDepartmentModelDraft>(
            department.id,
            departments.defaultFetcher
        ) { _, value ->
            value.globalBinding = includeAll
            value.regionBindings = emptyList()
            value.farmBindings = emptyList()
        }

        return departmentBindings(department.id)
    }

    override suspend fun modifyByRegionCriterion(
        departmentId: UUID,
        binding: ByRegionCriterion,
        plus: Boolean
    ): List<BindingCriterion> {
        val department = getDepartmentById(departmentId)
        val model = new(ByRegionCriterionModel::class).by {
            this.departmentId = departmentId
            this.regionId = binding.regionId
        }

        departments.save<OrganizationDepartmentModelDraft>(
            department.id,
            departments.defaultFetcher
        ) { _, value ->
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
        departmentId: UUID,
        binding: ByFarmCriterion,
        plus: Boolean
    ): List<BindingCriterion> {
        val department = getDepartmentById(departmentId)
        val model = new(ByFarmCriterionModel::class).by {
            this.departmentId = departmentId
            this.farmId = binding.farmId
            this.fieldIds = binding.fieldIds
        }

        departments.save<OrganizationDepartmentModelDraft>(
            department.id,
            departments.defaultFetcher
        ) { _, value ->
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
