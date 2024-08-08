package io.eordie.multimodule.api.tests.organizations

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.contracts.organization.models.UsersFilter
import org.junit.jupiter.api.Test

class UsersQueryTest : AbstractOrganizationTest() {

    @Test
    fun `should query users by membership`() = test(organizationManager) {
        val filter = UsersFilter(organization = OrganizationsFilter(name = StringLiteralFilter(eq = "Developers org")))
        val page = userQueries.users(filter)
        assertThat(page.pageable.cursor).isNull()
        assertThat(page.data.size).isAtLeast(2)
        assertThat(page.data.map { it.id }).containsExactly(developer1, developer2)
    }

    @Test
    fun `should load users by id`() = test(organizationManager) {
        val users = userQueries.loadUserByIds(listOf(developer1, developer2)).values
        assertThat(users).hasSize(2)
        assertThat(users.map { it.id }).containsExactly(developer1, developer2)
    }

    @Test
    fun `should load roles by user ids`() = test(organizationManager) {
        val roles = userQueries.loadRolesByUserIds(listOf(developer1, developer2), null)
        assertThat(roles).hasSize(2)
        assertThat(roles).containsExactly(
            developer1,
            emptyList<String>(),
            developer2,
            emptyList<String>()
        )
    }
}
