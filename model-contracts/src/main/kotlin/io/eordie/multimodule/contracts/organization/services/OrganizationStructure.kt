package io.eordie.multimodule.contracts.organization.services

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.annotations.Secured
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.organization.models.acl.BindingCriterion
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartmentFilter
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartmentInput
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployee
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeFilter
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeInput
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPositionFilter
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPositionInput
import io.eordie.multimodule.contracts.utils.Roles
import java.util.*

@AutoService(Query::class)
@Secured(oneOf = [ Roles.VIEW_ORGANIZATION, Roles.VIEW_ORGANIZATIONS ])
interface OrganizationStructureQueries : Query {
    suspend fun positions(
        @GraphQLIgnore currentOrganization: CurrentOrganization,
        filter: OrganizationPositionFilter? = null
    ): List<OrganizationPosition>

    suspend fun loadSubordinates(parentIds: List<UUID>): Map<UUID, List<OrganizationPosition>>

    suspend fun loadBindingsByDepartments(departmentIds: List<UUID>): Map<UUID, List<BindingCriterion>>

    suspend fun departments(
        @GraphQLIgnore currentOrganization: CurrentOrganization,
        filter: OrganizationDepartmentFilter? = null,
        pageable: Pageable? = null
    ): Page<OrganizationDepartment>

    suspend fun employees(
        @GraphQLIgnore currentOrganization: CurrentOrganization,
        filter: OrganizationEmployeeFilter? = null,
        pageable: Pageable? = null
    ): Page<OrganizationEmployee>
}

@AutoService(Mutation::class)
@Secured(oneOf = [ Roles.MANAGE_ORGANIZATION, Roles.MANAGE_ORGANIZATIONS ])
interface OrganizationStructureMutations : Mutation {

    suspend fun position(
        @GraphQLIgnore currentOrganization: CurrentOrganization,
        position: OrganizationPositionInput
    ): OrganizationPosition

    suspend fun deletePosition(positionId: UUID): Boolean

    suspend fun changePositionsParent(previousParentId: UUID, newParentId: UUID?): Boolean

    suspend fun deleteDepartment(departmentId: UUID): Boolean

    suspend fun department(
        @GraphQLIgnore currentOrganization: CurrentOrganization,
        input: OrganizationDepartmentInput
    ): OrganizationDepartment

    @Secured(value = [ Roles.MANAGE_MEMBERS ])
    suspend fun employee(
        @GraphQLIgnore currentOrganization: CurrentOrganization,
        employeeInput: OrganizationEmployeeInput
    ): OrganizationEmployee

    @Secured(value = [ Roles.MANAGE_MEMBERS ])
    suspend fun deleteEmployee(
        @GraphQLIgnore currentOrganization: CurrentOrganization,
        departmentId: UUID,
        userId: UUID
    ): Boolean
}
