package io.eordie.multimodule.api.tests.organizations

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.User
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import java.util.*

class OrganizationEntityLoaderTest : AbstractOrganizationTest() {

    @Inject
    lateinit var users: EntityLoader<User, UUID>

    @Inject
    lateinit var organizations: EntityLoader<Organization, UUID>

    @Test
    fun `should load users by loader`() = test(organizationManager) {
        val userList = users.load(listOf(developer1, developer2)).values
        assertThat(userList).hasSize(2)
    }

    @Test
    fun `should load organization by loader`() = test(organizationManager) {
        val organization = organizations.load(listOf(developersOrg.id)).values.single()
        assertThat(organization.id).isEqualTo(developersOrg.id)
        assertThat(organization.name).isNotNull()
    }

    @Test
    fun `should load organization permissions by loader`() = test(organizationManager) {
        val permissions = organizations.loadPermissions(listOf(developersOrg.id)).values.single()
        assertThat(permissions).isNotEmpty()
        assertThat(permissions).hasSize(1)
        assertThat(permissions).containsExactly(Permission.VIEW)
    }
}
