package io.eordie.multimodule.api.tests.organizations

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.api.tests.AuthUtils.authWith
import io.eordie.multimodule.contracts.organization.models.acl.ByFarmCriterion
import io.eordie.multimodule.contracts.organization.models.acl.FarmAcl
import io.eordie.multimodule.contracts.organization.models.acl.FarmAclInput
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.contracts.organization.services.FarmAclMutations
import io.eordie.multimodule.contracts.organization.services.FarmAclQueries
import io.eordie.multimodule.contracts.utils.Roles.MANAGE_MEMBERS
import io.eordie.multimodule.contracts.utils.Roles.MANAGE_ORGANIZATION
import io.eordie.multimodule.contracts.utils.Roles.VIEW_ORGANIZATION
import jakarta.inject.Inject
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class FarmAclTest : AbstractOrganizationTest() {

    @Inject
    lateinit var queries: FarmAclQueries

    @Inject
    lateinit var mutations: FarmAclMutations

    private lateinit var department: OrganizationDepartment
    private lateinit var structure: Map<String, OrganizationPosition>

    private val firstManager = authWith(firstOrg, MANAGE_ORGANIZATION)
    private val secondManager = authWith(secondOrg, MANAGE_ORGANIZATION, VIEW_ORGANIZATION, MANAGE_MEMBERS)
    private val secondUser = authWith {
        copy(userId = developer2, currentOrganizationId = secondOrg.id)
    }

    @BeforeEach
    fun init() = runTest {
        withContext(firstManager) {
            queries.queryFarmAcl().data.forEach {
                mutations.deleteFarmAcl(it.id)
            }
        }
        withContext(secondManager) {
            truncatePositions(secondOrg)
            truncateDepartments(secondOrg)
            structure = createExampleStructure(secondOrg)
            department = newDepartment(secondOrg, "Basic")
        }
    }

    @Test
    fun `mutations on farm acl should affect end user by farm binding`() = runTest {
        val farmId = UUID.randomUUID()
        val fieldIds = listOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        val binding = ByFarmCriterion(farmId, fieldIds)

        newEmployeeAndAcl(farmId, fieldIds)

        // attach department by farm binding
        withContext(secondManager) {
            departmentMutations.modifyByFarmCriterion(secondOrg, department.id, binding, true)
        }

        // verify user has required acl
        withContext(secondUser) {
            val acl = employeeAclQueries.currentEmployeeAcl()
            assertThat(acl).hasSize(1)
            assertThat(acl[0].farmId).isEqualTo(farmId)
            assertThat(acl[0].fieldIds).hasSize(3)
            assertThat(acl[0].fieldIds).isEqualTo(fieldIds)
        }

        // change binding
        withContext(secondManager) {
            val updatedBinding = binding.copy(fieldIds = fieldIds.take(2))
            departmentMutations.modifyByFarmCriterion(secondOrg, department.id, updatedBinding, true)
        }

        // verify user has required acl
        withContext(secondUser) {
            val acl = employeeAclQueries.currentEmployeeAcl()
            assertThat(acl).hasSize(1)
            assertThat(acl[0].farmId).isEqualTo(farmId)
            assertThat(acl[0].fieldIds).hasSize(2)
            assertThat(acl[0].fieldIds).isEqualTo(fieldIds.take(2))
        }

        // remove farm binding
        withContext(secondManager) {
            departmentMutations.modifyByFarmCriterion(secondOrg, department.id, binding, false)
        }

        // verify user has empty acl
        withContext(secondUser) {
            assertThat(employeeAclQueries.currentEmployeeAcl()).isEmpty()
        }
    }

    @Test
    fun `mutations on farm acl should affect end user by global binding`() = runTest {
        val farmId = UUID.randomUUID()
        val fieldIds = listOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())

        newEmployeeAndAcl(farmId, fieldIds)

        // attach department by global binding
        withContext(secondManager) {
            departmentMutations.modifyGlobalBinding(secondOrg, department.id, true)
        }

        // verify user has required acl
        withContext(secondUser) {
            val acl = employeeAclQueries.currentEmployeeAcl()
            assertThat(acl).hasSize(1)
            assertThat(acl[0].farmId).isEqualTo(farmId)
            assertThat(acl[0].fieldIds).isEqualTo(fieldIds)
        }

        // remove global binding
        withContext(secondManager) {
            departmentMutations.modifyGlobalBinding(secondOrg, department.id, false)
        }

        // verify user has empty acl
        withContext(secondUser) {
            assertThat(employeeAclQueries.currentEmployeeAcl()).isEmpty()
        }
    }

    private suspend fun newEmployeeAndAcl(farmId: UUID, fieldIds: List<UUID>) {
        // create new employee
        withContext(secondManager) {
            val position = structure.getValue("Junior Developer")
            newEmployee(secondOrg, secondUser.details.userId, department, position)
        }

        // create new acl for organization
        withContext(firstManager) {
            mutations.farmAcl(firstOrg, FarmAclInput(null, secondOrg.id, farmId, emptyList(), fieldIds))
        }

        // acl is not attached to any departments
        withContext(secondUser) {
            assertThat(employeeAclQueries.currentEmployeeAcl()).isEmpty()
        }
    }

    @Test
    fun `should create farm acl for foreign organization`() = runTest {
        val farmId = UUID.randomUUID()
        fun assertEquals(acl: FarmAcl) {
            assertThat(acl.id).isNotNull()
            assertThat(acl.farmId).isEqualTo(farmId)
            assertThat(acl.organizationId).isEqualTo(secondOrg.id)
            assertThat(acl.roles).isEmpty()
        }

        withContext(firstManager) {
            val acl = mutations.farmAcl(firstOrg, FarmAclInput(null, secondOrg.id, farmId, emptyList()))
            assertEquals(acl)
            assertThat(acl.permissions).hasSize(3)

            val data = queries.queryFarmAcl().data
            assertThat(data).hasSize(1)
            assertEquals(data[0])
            assertThat(data[0].permissions).hasSize(3)
        }

        withContext(secondManager) {
            val data = queries.queryFarmAcl().data
            assertThat(data).hasSize(1)
            assertEquals(data[0])
            assertThat(data[0].permissions).hasSize(1)
        }
    }
}
