package io.eordie.multimodule.api.tests.organizations

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.contracts.basic.exception.AccessDeniedException
import io.eordie.multimodule.contracts.utils.Roles.MANAGE_ORGANIZATION
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrganizationDepartmentsTest : AbstractOrganizationTest() {

    @BeforeEach
    fun init() = test(organizationManager) {
        truncateDepartments(developersOrg)
        truncatePositions(developersOrg)
    }

    @Test
    fun `should create single department`() = test(organizationManager) {
        newDepartment(developersOrg, "dev")
        val (data, _) = structureQueries.departments(developersOrg)
        assertThat(data).hasSize(1)
        assertThat(data[0].name).isEqualTo("dev")
    }

    @Test
    fun `should delete department`() = test(organizationManager) {
        newDepartment(developersOrg, "dev")
        var (data, _) = structureQueries.departments(developersOrg)
        assertThat(data).hasSize(1)
        assertThat(data[0].name).isEqualTo("dev")

        structureMutations.deleteDepartment(data[0].id)
        data = structureQueries.departments(developersOrg).data
        assertThat(data).isEmpty()
    }

    @Test
    fun `should not create department without role`() = test(organizationManager - MANAGE_ORGANIZATION) {
        assertThrows<AccessDeniedException> { newDepartment(developersOrg, "dev") }
    }
}
