package io.eordie.multimodule.api.tests.organizations

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.contracts.organization.models.UsersFilter
import io.eordie.multimodule.contracts.organization.services.UserQueries
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

class UsersQueryTest : AbstractOrganizationTest() {

    @Inject
    lateinit var userQueries: UserQueries

    @Test
    fun `should query users by membership`() = test(organizationManager) {
        val filter = UsersFilter(organization = OrganizationsFilter(name = StringLiteralFilter(eq = "Developers org")))
        val page = userQueries.users(filter)
        assertThat(page.pageable.cursor).isNull()
        assertThat(page.data).hasSize(2)
        assertThat(page.data.map { it.id }).containsExactly(developer1, developer2)
    }
}
