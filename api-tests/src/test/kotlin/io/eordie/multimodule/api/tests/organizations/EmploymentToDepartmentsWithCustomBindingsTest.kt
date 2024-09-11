package io.eordie.multimodule.api.tests.organizations

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.api.tests.AuthUtils.authWith
import io.eordie.multimodule.contracts.basic.filters.Direction
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.contracts.organization.models.UsersFilter
import io.eordie.multimodule.contracts.organization.models.acl.ByFarmCriterion
import io.eordie.multimodule.contracts.organization.models.acl.EmployeeAcl
import io.eordie.multimodule.contracts.organization.models.acl.FarmAcl
import io.eordie.multimodule.contracts.organization.models.acl.FarmAclFilter
import io.eordie.multimodule.contracts.organization.models.acl.FarmAclInput
import io.eordie.multimodule.contracts.organization.models.acl.GlobalCriterion
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartmentFilter
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeFilter
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployeeInput
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPositionFilter
import io.eordie.multimodule.contracts.utils.Roles.MANAGE_ORGANIZATION
import io.eordie.multimodule.contracts.utils.Roles.VIEW_ORGANIZATION
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.util.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class EmploymentToDepartmentsWithCustomBindingsTest : AbstractOrganizationTest() {

    private val farmOwner = authWith(firstOrg, MANAGE_ORGANIZATION)

    private lateinit var farmAcl: List<FarmAcl>
    private lateinit var empty: OrganizationDepartment
    private lateinit var specific: OrganizationDepartment
    private lateinit var fullAccess: OrganizationDepartment

    @Test
    @Order(10)
    fun `should create required farm acl`() {
        suspend fun createAcl(farmId: UUID): FarmAcl {
            val fieldIds = listOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
            val input = FarmAclInput(null, developersOrg.id, farmId, emptyList(), fieldIds)
            val acl = farmAclMutations.farmAcl(firstOrg, input)
            assertThat(acl.id).isNotNull()
            assertThat(acl.farmId).isEqualTo(farmId)
            assertThat(acl.organizationId).isEqualTo(developersOrg.id)
            assertThat(acl.farmOwnerOrganizationId).isEqualTo(firstOrg.id)
            return acl
        }

        test(farmOwner) {
            farmAcl = listOf(createAcl(UUID.randomUUID()), createAcl(UUID.randomUUID()))
        }

        test(organizationManager) {
            val data = farmAclQueries.queryFarmAcl(FarmAclFilter(direction = Direction.INCOME)).data
            assertThat(data).hasSize(2)
        }
    }

    @Test
    @Order(20)
    fun `should create department with full access`() = test(organizationManager) {
        fullAccess = newDepartment(developersOrg, "Full Access")
        val fullAccessBindings = departmentMutations.modifyGlobalBinding(developersOrg, fullAccess.id, true)
        assertThat(fullAccessBindings).hasSize(1)
        assertThat(fullAccessBindings).containsExactly(GlobalCriterion(true))
    }

    @Test
    @Order(21)
    fun `should create department with empty access`() = test(organizationManager) {
        empty = newDepartment(developersOrg, "Empty Access")
        val emptyAccessBindings = departmentMutations.modifyGlobalBinding(developersOrg, empty.id, false)
        assertThat(emptyAccessBindings).hasSize(1)
        assertThat(emptyAccessBindings).containsExactly(GlobalCriterion(false))
    }

    @Test
    @Order(22)
    fun `should create department with specific access`() = test(organizationManager) {
        specific = newDepartment(developersOrg, "Specific access")
        farmAcl.forEach { acl ->
            val criterion = ByFarmCriterion(acl.farmId, acl.fieldIds)
            departmentMutations.modifyByFarmCriterion(developersOrg, specific.id, criterion, true)
        }

        val bindings = structureQueries.loadBindingsByDepartments(listOf(specific.id)).values.first()
        assertThat(bindings).hasSize(farmAcl.size)
        bindings.forEach {
            assertThat(it).isInstanceOf(ByFarmCriterion::class.java)
        }
    }

    @Test
    @Order(23)
    fun `duplicate department creation shouldn't create duplicate`() = test(organizationManager) {
        farmAcl.forEach { acl ->
            val criterion = ByFarmCriterion(acl.farmId, acl.fieldIds)
            departmentMutations.modifyByFarmCriterion(developersOrg, specific.id, criterion, true)
        }

        val bindings = structureQueries.loadBindingsByDepartments(listOf(specific.id)).values.first()
        assertThat(bindings).hasSize(farmAcl.size)
        bindings.forEach {
            assertThat(it).isInstanceOf(ByFarmCriterion::class.java)
        }
    }

    @Test
    @Order(30)
    fun `should apply members to departments`() = test(organizationManager) {
        val filter = UsersFilter(
            hasEmployee = false,
            organization = OrganizationsFilter(id = UUIDLiteralFilter(eq = developersOrg.id))
        )
        val users = userQueries.users(filter).data
        assertThat(users.size).isAtLeast(2)
        assertThat(users.map { it.id }).containsAtLeast(developer1, developer2)

        val structure = createExampleStructure(developersOrg)
        val junior = structure.getValue("Junior Developer")
        structureMutations.employee(developersOrg, OrganizationEmployeeInput(developer1, fullAccess.id, junior.id))
        structureMutations.employee(developersOrg, OrganizationEmployeeInput(developer2, specific.id, junior.id))
    }

    @Test
    @Order(31)
    fun `query users by employee filter`() = test(organizationManager) {
        val filter = UsersFilter(
            hasEmployee = true,
            organization = OrganizationsFilter(id = UUIDLiteralFilter(eq = developersOrg.id))
        )

        val users = userQueries.users(filter).data
        assertThat(users).hasSize(2)
        assertThat(users.map { it.id }).containsExactly(developer1, developer2)
    }

    @Test
    @Order(31)
    fun `query users by department filter - full access department`() = test(organizationManager) {
        val filter = UsersFilter(
            employee = OrganizationEmployeeFilter(
                department = OrganizationDepartmentFilter(id = UUIDLiteralFilter(eq = fullAccess.id))
            ),
            organization = OrganizationsFilter(id = UUIDLiteralFilter(eq = developersOrg.id))
        )

        val users = userQueries.users(filter).data
        assertThat(users).hasSize(1)
        assertThat(users.map { it.id }).containsExactly(developer1)
    }

    @Test
    @Order(31)
    fun `query users by department filter - specific access department`() = test(organizationManager) {
        val filter = UsersFilter(
            employee = OrganizationEmployeeFilter(
                department = OrganizationDepartmentFilter(id = UUIDLiteralFilter(eq = specific.id))
            ),
            organization = OrganizationsFilter(id = UUIDLiteralFilter(eq = developersOrg.id))
        )

        val users = userQueries.users(filter).data
        assertThat(users).hasSize(1)
        assertThat(users.map { it.id }).containsExactly(developer2)
    }

    @Test
    @Order(31)
    fun `query users by position filter`() = test(organizationManager) {
        val filter = UsersFilter(
            employee = OrganizationEmployeeFilter(
                position = OrganizationPositionFilter(name = StringLiteralFilter(like = "Junior"))
            ),
            organization = OrganizationsFilter(id = UUIDLiteralFilter(eq = developersOrg.id))
        )

        val users = userQueries.users(filter).data
        assertThat(users).hasSize(2)
        assertThat(users.map { it.id }).containsExactly(developer1, developer2)
    }

    @Test
    @Order(31)
    fun `query users by department accessible farm ids`() = test(organizationManager) {
        val filter = UsersFilter(
            employee = OrganizationEmployeeFilter(
                department = OrganizationDepartmentFilter(
                    farmId = UUIDLiteralFilter(of = farmAcl.map { it.farmId })
                )
            )
        )

        val users = userQueries.users(filter).data
        assertThat(users).hasSize(2)
        assertThat(users.map { it.id }).containsExactly(developer1, developer2)
    }

    @Test
    @Order(31)
    fun `query users by department accessible farm field ids`() = test(organizationManager) {
        val filter = UsersFilter(
            employee = OrganizationEmployeeFilter(
                department = OrganizationDepartmentFilter(
                    fieldId = UUIDLiteralFilter(of = farmAcl.mapNotNull { it.fieldIds }.flatten())
                )
            )
        )

        val users = userQueries.users(filter).data
        assertThat(users).hasSize(2)
        assertThat(users.map { it.id }).containsExactly(developer1, developer2)
    }

    @Test
    @Order(40)
    fun `verify full access to available farms`() = test(organizationManager) {
        val acl = employeeAclQueries.internalActiveResourceAcl(developer1, developersOrg.id)
        assertThat(acl).isNotNull()
        assertThat(acl?.auth?.roles).containsExactly(VIEW_ORGANIZATION)

        val entries = acl?.entries.orEmpty()
        assertThat(entries).hasSize(2)
        assertThat(entries[0].farmId).isAnyOf(farmAcl[0].farmId, farmAcl[1].farmId)
        assertThat(entries[0].fieldIds).hasSize(3)

        assertThat(entries[1].farmId).isAnyOf(farmAcl[0].farmId, farmAcl[1].farmId)
        assertThat(entries[1].fieldIds).hasSize(3)
    }

    @Test
    @Order(41)
    fun `verify specific access to available farms`() = test(organizationManager) {
        val acl = employeeAclQueries.internalActiveResourceAcl(developer2, developersOrg.id)
        assertThat(acl).isNotNull()
        assertThat(acl?.auth?.roles).containsExactly(VIEW_ORGANIZATION)

        val entries = acl?.entries.orEmpty()
        assertThat(entries).hasSize(2)
        assertThat(entries[0].farmId).isAnyOf(farmAcl[0].farmId, farmAcl[1].farmId)
        assertThat(entries[0].fieldIds).hasSize(3)

        assertThat(entries[1].farmId).isAnyOf(farmAcl[0].farmId, farmAcl[1].farmId)
        assertThat(entries[1].fieldIds).hasSize(3)
    }

    @Test
    @Order(50)
    fun `verify access after reduce fields from farm acl`() {
        test(farmOwner) {
            farmAcl.forEach {
                val input = FarmAclInput(it.id, it.organizationId, it.farmId, emptyList(), it.fieldIds?.take(2))
                farmAclMutations.farmAcl(firstOrg, input)
            }
        }

        test(organizationManager) {
            fun ensureEntries(entries: List<EmployeeAcl>?) {
                assertThat(entries).isNotNull()
                assertThat(entries!!).hasSize(2)
                assertThat(entries[0].farmId).isAnyOf(farmAcl[0].farmId, farmAcl[1].farmId)
                assertThat(entries[0].fieldIds).hasSize(2)

                assertThat(entries[1].farmId).isAnyOf(farmAcl[0].farmId, farmAcl[1].farmId)
                assertThat(entries[1].fieldIds).hasSize(2)
            }

            ensureEntries(employeeAclQueries.internalActiveResourceAcl(developer1, developersOrg.id)?.entries)
            ensureEntries(employeeAclQueries.internalActiveResourceAcl(developer2, developersOrg.id)?.entries)
        }
    }

    @Test
    @Order(60)
    fun `verify access after remove farm acl`() {
        test(farmOwner) {
            farmAclMutations.deleteFarmAcl(farmAcl[0].id)
        }

        test(organizationManager) {
            fun ensureEntries(entries: List<EmployeeAcl>?) {
                assertThat(entries).isNotNull()
                assertThat(entries!!).hasSize(1)
                assertThat(entries[0].farmId).isEqualTo(farmAcl[1].farmId)
                assertThat(entries[0].fieldIds).hasSize(2)
            }

            ensureEntries(employeeAclQueries.internalActiveResourceAcl(developer1, developersOrg.id)?.entries)
            ensureEntries(employeeAclQueries.internalActiveResourceAcl(developer2, developersOrg.id)?.entries)
        }
    }

    @Test
    @Order(70)
    fun `verify access after position remove`() = test(organizationManager) {
        val filter = OrganizationPositionFilter(name = StringLiteralFilter(eq = "Junior Developer"))
        val positions = structureQueries.positions(developersOrg, filter)
        assertThat(positions).hasSize(1)
        structureMutations.deletePosition(positions[0].id)

        assertThat(employeeAclQueries.internalActiveResourceAcl(developer1, developersOrg.id)?.entries).isNull()
        assertThat(employeeAclQueries.internalActiveResourceAcl(developer2, developersOrg.id)?.entries).isNull()
    }
}
