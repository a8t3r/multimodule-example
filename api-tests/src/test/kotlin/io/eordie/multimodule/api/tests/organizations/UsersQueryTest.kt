package io.eordie.multimodule.api.tests.organizations

import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.containsOnly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.contracts.organization.models.UsersFilter
import org.junit.jupiter.api.Test

class UsersQueryTest : AbstractOrganizationTest() {

    @Test
    fun `should query users by membership`() = test(organizationManager) {
        val page = userQueries.users(
            UsersFilter(
                organization = OrganizationsFilter(name = StringLiteralFilter(eq = "Developers org"))
            )
        )

        assertThat(page.pageable.cursor).isNull()
        assertThat(page.data.size).isGreaterThanOrEqualTo(2)
        assertThat(page.data.map { it.id }).containsAtLeast(developer1, developer2)
    }

    @Test
    fun `should query unemployed users`() = test(organizationManager) {
        val page = userQueries.users(
            UsersFilter(
                hasEmployee = false,
                organization = OrganizationsFilter(name = StringLiteralFilter(eq = "Developers org"))
            )
        )

        assertThat(page.pageable.cursor).isNull()
        assertThat(page.data.size).isGreaterThanOrEqualTo(2)
        assertThat(page.data.map { it.id }).containsAtLeast(developer1, developer2)
    }

    @Test
    fun `should query employed users`() = test(organizationManager) {
        val page = userQueries.users(
            UsersFilter(
                hasEmployee = true,
                organization = OrganizationsFilter(name = StringLiteralFilter(eq = "Developers org"))
            )
        )

        assertThat(page.pageable.cursor).isNull()
        assertThat(page.data).isEmpty()
    }

    @Test
    fun `should load users by id`() = test(organizationManager) {
        val users = userQueries.loadUserByIds(listOf(developer1, developer2)).values
        assertThat(users.size).isGreaterThanOrEqualTo(2)
        assertThat(users.map { it.id }).containsAtLeast(developer1, developer2)
    }

    @Test
    fun `should load roles by user ids`() = test(organizationManager) {
        val roles = userQueries.loadRolesByUserIds(listOf(developer1, developer2), null)
        assertThat(roles).hasSize(2)
        assertThat(roles).isNotNull().containsOnly(
            developer1 to emptyList<String>(),
            developer2 to emptyList<String>()
        )
    }
}
