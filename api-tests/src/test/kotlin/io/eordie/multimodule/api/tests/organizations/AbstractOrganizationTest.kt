package io.eordie.multimodule.api.tests.organizations

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.api.tests.AbstractApplicationTest
import io.eordie.multimodule.api.tests.AuthUtils.authWith
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartmentInput
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployee
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeInput
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPositionFilter
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPositionInput
import io.eordie.multimodule.contracts.organization.services.EmployeeAclQueries
import io.eordie.multimodule.contracts.organization.services.OrganizationDepartmentMutations
import io.eordie.multimodule.contracts.organization.services.OrganizationStructureMutations
import io.eordie.multimodule.contracts.organization.services.OrganizationStructureQueries
import io.eordie.multimodule.contracts.utils.Roles
import io.micronaut.test.annotation.Sql
import jakarta.inject.Inject
import org.junit.jupiter.api.parallel.ResourceLock
import java.util.*

@ResourceLock("structureQueries")
@Sql("initial-keycloak.sql", phase = Sql.Phase.BEFORE_ALL)
abstract class AbstractOrganizationTest : AbstractApplicationTest() {
    companion object {
        val organizationManager = authWith(
            developersOrg,
            Roles.MANAGE_ORGANIZATION,
            Roles.VIEW_ORGANIZATION,
            Roles.VIEW_MEMBERS,
            Roles.MANAGE_MEMBERS
        )
    }

    @Inject
    lateinit var structureQueries: OrganizationStructureQueries

    @Inject
    lateinit var structureMutations: OrganizationStructureMutations

    @Inject
    lateinit var employeeAclQueries: EmployeeAclQueries

    @Inject
    lateinit var departmentMutations: OrganizationDepartmentMutations

    protected suspend fun truncateDepartments(currentOrganization: CurrentOrganization) =
        structureQueries.departments(currentOrganization)
            .data.forEach { structureMutations.deleteDepartment(it.id) }

    protected suspend fun truncatePositions(currentOrganization: CurrentOrganization) =
        structureQueries.positions(currentOrganization)
            .forEach { structureMutations.deletePosition(it.id) }

    protected suspend fun newPosition(
        currentOrganization: CurrentOrganization,
        name: String,
        parent: OrganizationPosition?
    ): OrganizationPosition =
        structureMutations.position(
            currentOrganization,
            OrganizationPositionInput(
                null,
                name,
                listOf(Roles.VIEW_ORGANIZATION.humanName()),
                parent?.id
            )
        )

    suspend fun newDepartment(currentOrganization: CurrentOrganization, name: String): OrganizationDepartment =
        structureMutations.department(currentOrganization, OrganizationDepartmentInput(null, name))

    suspend fun newEmployee(
        currentOrganization: CurrentOrganization,
        userId: UUID,
        department: OrganizationDepartment,
        position: OrganizationPosition
    ): OrganizationEmployee {
        val input = OrganizationEmployeeInput(userId, department.id, position.id)
        return structureMutations.employee(currentOrganization, input)
    }

    protected suspend fun createExampleStructure(currentOrganization: CurrentOrganization): Map<String, OrganizationPosition> {
        val ctoPosition = newPosition(currentOrganization, "CTO", null)
        val seniorPosition = newPosition(currentOrganization, "Senior Developer", ctoPosition)
        val qaSeniorPosition = newPosition(currentOrganization, "QA Senior Developer", ctoPosition)
        val juniorPosition = newPosition(currentOrganization, "Junior Developer", seniorPosition)

        val positions = structureQueries.positions(currentOrganization, OrganizationPositionFilter())
        assertThat(positions).hasSize(4)
        assertThat(positions.map { it.name to it.parentId }).containsExactly(
            ctoPosition.name to null,
            seniorPosition.name to ctoPosition.id,
            qaSeniorPosition.name to ctoPosition.id,
            juniorPosition.name to seniorPosition.id
        )
        return positions.associateBy { it.name }
    }
}
