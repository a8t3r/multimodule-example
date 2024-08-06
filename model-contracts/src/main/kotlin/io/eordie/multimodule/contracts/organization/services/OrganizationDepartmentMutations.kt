package io.eordie.multimodule.contracts.organization.services

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.annotations.Valid
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.organization.models.acl.BindingCriterion
import io.eordie.multimodule.contracts.organization.models.acl.ByFarmCriterion
import io.eordie.multimodule.contracts.organization.models.acl.ByRegionCriterion
import java.util.*

@AutoService(Mutation::class)
interface OrganizationDepartmentMutations : Mutation {

    suspend fun modifyGlobalBinding(
        @GraphQLIgnore currentOrganization: CurrentOrganization,
        departmentId: UUID,
        includeAll: Boolean
    ): List<BindingCriterion>

    suspend fun modifyByRegionCriterion(
        @GraphQLIgnore currentOrganization: CurrentOrganization,
        departmentId: UUID,
        binding: ByRegionCriterion,
        plus: Boolean
    ): List<BindingCriterion>

    suspend fun modifyByFarmCriterion(
        @GraphQLIgnore currentOrganization: CurrentOrganization,
        departmentId: UUID,
        @Valid binding: ByFarmCriterion,
        plus: Boolean
    ): List<BindingCriterion>
}
