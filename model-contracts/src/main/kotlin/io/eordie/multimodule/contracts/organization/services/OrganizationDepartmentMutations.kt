package io.eordie.multimodule.contracts.organization.services

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.annotations.Valid
import io.eordie.multimodule.contracts.organization.models.acl.BindingCriterion
import io.eordie.multimodule.contracts.organization.models.acl.ByFarmCriterion
import io.eordie.multimodule.contracts.organization.models.acl.ByRegionCriterion
import java.util.*

@AutoService(Mutation::class)
interface OrganizationDepartmentMutations : Mutation {

    suspend fun modifyGlobalBinding(
        departmentId: UUID,
        includeAll: Boolean
    ): List<BindingCriterion>

    suspend fun modifyByRegionCriterion(
        departmentId: UUID,
        binding: ByRegionCriterion,
        plus: Boolean
    ): List<BindingCriterion>

    suspend fun modifyByFarmCriterion(
        departmentId: UUID,
        @Valid binding: ByFarmCriterion,
        plus: Boolean
    ): List<BindingCriterion>
}
