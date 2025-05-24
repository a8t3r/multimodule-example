package io.eordie.multimodule.api.tests.organizations

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.extracting
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isIn
import assertk.assertions.isNull
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployee
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPosition
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrganizationEmployeesTest : AbstractOrganizationTest() {

    private lateinit var department: OrganizationDepartment
    private lateinit var structure: Map<String, OrganizationPosition>

    @BeforeEach
    fun init() = test(organizationManager) {
        truncateDepartments(developersOrg)
        truncatePositions(developersOrg)
        structure = createExampleStructure(developersOrg)
        department = newDepartment(developersOrg, "Basic")
    }

    @Test
    fun `should create new employee`() = test(organizationManager) {
        val position = structure.getValue("Senior Developer")

        newEmployee(developersOrg, developer1, department, position)

        val employees = structureQueries.employees(developersOrg).data
        assertThat(employees).hasSize(1)
        val employee = employees[0]
        assertThat(employee.organizationId).isEqualTo(developersOrg.id)
        assertThat(employee.departmentId).isEqualTo(department.id)
        assertThat(employee.positionId).isEqualTo(position.id)
        assertThat(employee.userId).isEqualTo(developer1)
    }

    @Test
    fun `should create new employee without department`() = test(organizationManager) {
        val position = structure.getValue("Senior Developer")

        newEmployee(developersOrg, developer1, null, position)

        val employees = structureQueries.employees(developersOrg).data
        assertThat(employees).hasSize(1)
        val employee = employees[0]
        assertThat(employee.organizationId).isEqualTo(developersOrg.id)
        assertThat(employee.departmentId).isNull()
        assertThat(employee.positionId).isEqualTo(position.id)
        assertThat(employee.userId).isEqualTo(developer1)
    }

    @Test
    fun `should create new employee with two departments`() = test(organizationManager) {
        val position = structure.getValue("Senior Developer")

        newEmployee(developersOrg, developer1, null, position)
        newEmployee(developersOrg, developer1, department, position)

        val employees = structureQueries.employees(developersOrg).data
        assertThat(employees).hasSize(2)

        assertThat(employees[0].organizationId).isEqualTo(developersOrg.id)
        assertThat(employees[0].departmentId).isIn(null, department.id)
        assertThat(employees[0].positionId).isEqualTo(position.id)
        assertThat(employees[0].userId).isEqualTo(developer1)

        assertThat(employees[1].organizationId).isEqualTo(developersOrg.id)
        assertThat(employees[0].departmentId).isIn(null, department.id)
        assertThat(employees[1].positionId).isEqualTo(position.id)
        assertThat(employees[1].userId).isEqualTo(developer1)
    }

    @Test
    fun `should delete employee`() = test(organizationManager) {
        val position = structure.getValue("Senior Developer")
        val employee = newEmployee(developersOrg, developer1, department, position)

        var employees = structureQueries.employees(developersOrg).data
        assertThat(employees).hasSize(1)

        structureMutations.deleteEmployee(developersOrg, employee.departmentId, employee.userId)

        employees = structureQueries.employees(developersOrg).data
        assertThat(employees).isEmpty()
    }

    @Test
    fun `should dissociate employees after department deletion`() = test(organizationManager) {
        val position = structure.getValue("Senior Developer")
        val employee1 = newEmployee(developersOrg, developer1, department, position)
        val employee2 = newEmployee(developersOrg, developer2, department, position)

        var employees = structureQueries.employees(developersOrg).data
        assertThat(employees).hasSize(2)
        assertThat(employees).extracting(OrganizationEmployee::userId).containsExactlyInAnyOrder(
            employee1.userId,
            employee2.userId
        )

        structureMutations.deleteDepartment(department.id)

        employees = structureQueries.employees(developersOrg).data
        assertThat(employees).isEmpty()
    }

    @Test
    fun `should dissociate employees after position deletion`() = test(organizationManager) {
        val employee1 = newEmployee(developersOrg, developer1, department, structure.getValue("Senior Developer"))
        val employee2 = newEmployee(developersOrg, developer2, department, structure.getValue("Junior Developer"))

        var employees = structureQueries.employees(developersOrg).data
        assertThat(employees).hasSize(2)
        assertThat(employees).extracting(OrganizationEmployee::userId).containsExactlyInAnyOrder(
            employee1.userId,
            employee2.userId
        )

        structureMutations.deletePosition(employee1.positionId)

        employees = structureQueries.employees(developersOrg).data
        assertThat(employees).hasSize(1)
        assertThat(employees[0].userId).isEqualTo(employee2.userId)
    }
}
