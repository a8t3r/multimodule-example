package io.eordie.multimodule.api.tests.organizations

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.eordie.multimodule.api.tests.AuthUtils.authWith
import io.eordie.multimodule.contracts.basic.exception.ValidationException
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.organization.OrganizationInput
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import kotlinx.coroutines.future.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrganizationTest : AbstractOrganizationTest() {

    companion object {
        private val firstUser = authWith(firstOrg, developer1)
        private val secondUser = authWith(secondOrg, developer2)
    }

    @Test
    fun `prohibit mutation in an organization by key`() = test(firstUser) {
        val e = assertThrows<ValidationException> {
            organizationMutations.organization(
                OrganizationInput(firstOrg.id, "Foobar")
            )
        }

        assertThat(e.errors).hasSize(1)
        assertThat(e.errors[0].constraint).isEqualTo("MissingPermission")
    }

    @Test
    fun `prohibit mutation in an organization by name`() = test(firstUser) {
        val e = assertThrows<ValidationException> {
            organizationMutations.organization(
                OrganizationInput(null, "First Organization")
            )
        }

        assertThat(e.errors).hasSize(1)
        assertThat(e.errors[0].constraint).isEqualTo("MissingPermission")
    }

    @Test
    fun `prohibit mutation in a third party organization by name`() = test(secondUser) {
        val e = assertThrows<ValidationException> {
            organizationMutations.organization(
                OrganizationInput(null, "First Organization")
            )
        }

        assertThat(e.errors).hasSize(1)
        assertThat(e.errors[0].constraint).isEqualTo("MissingPermission")
    }

    @Test
    fun `should create new organization`() = test(firstUser) {
        val organization = organizationMutations.organization(
            OrganizationInput(null, "My new Organization")
        )

        assertThat(organization).isNotNull()
        assertThat(organization.id).isNotNull()
        assertThat(organization.displayName).isNull()
        assertThat(organization.permissions).hasSize(3)
        assertThat(organization.name).isEqualTo("My new Organization")

        val filter = OrganizationsFilter(name = StringLiteralFilter(eq = organization.name))
        val page = organizationQueries.organizations(
            filter
        )

        assertThat(page.data).hasSize(1)
        assertThat(page.data[0].id).isEqualTo(organization.id)
        assertThat(page.data[0].permissions).hasSize(3)
        assertThat(page.data[0].name).isEqualTo("My new Organization")
    }

    @Test
    fun `should retrieve an organization digest`() = test(firstUser) {
        val organization = organizationMutations.organization(
            OrganizationInput(null, "Organization digest")
        )

        val digest = organization.digest(env()).await()
        assertThat(digest.organizationId).isEqualTo(organization.id)
        assertThat(digest.domainsCount).isEqualTo(0)
        assertThat(digest.positionsCount).isEqualTo(0)
        assertThat(digest.departmentsCount).isEqualTo(0)
        assertThat(digest.employeesCount).isEqualTo(0)
        assertThat(digest.membersCount).isEqualTo(2)
    }

    @Test
    fun `should retrieve a filter summary`() = test(firstUser) {
        val organization = organizationMutations.organization(
            OrganizationInput(null, "Filter summary")
        )

        val filterSummary = organizationQueries.organizationSummary(
            OrganizationsFilter(name = StringLiteralFilter(eq = organization.name))
        )

        assertThat(filterSummary.totalCount).isEqualTo(1)
        assertThat(filterSummary.organizationIds).hasSize(1)
        assertThat(filterSummary.organizationIds).containsExactly(organization.id)
        assertThat(filterSummary.domainIds).isEmpty()
        assertThat(filterSummary.userIds).hasSize(2)
    }

    @Test
    fun `should update new organization`() = test(firstUser) {
        val organization = organizationMutations.organization(
            OrganizationInput(null, "Just another new organization")
        )
        assertThat(organization.id).isNotNull()
        assertThat(organization.displayName).isNull()
        assertThat(organization.permissions).hasSize(3)
        assertThat(organization.name).isEqualTo("Just another new organization")

        val actual = organizationMutations.organization(
            OrganizationInput(organization.id, "Updated name", "Updated display")
        )
        assertThat(actual.id).isEqualTo(organization.id)
        assertThat(actual.displayName).isEqualTo("Updated display")
        assertThat(actual.permissions).hasSize(3)
        assertThat(actual.name).isEqualTo("Updated name")
    }

    @Test
    fun `prohibit deletion of a third party organization`() = test(firstUser) {
        val e = assertThrows<ValidationException> {
            organizationMutations.deleteOrganization(firstOrg.id)
        }
        assertThat(e.errors).hasSize(1)
        assertThat(e.errors[0].constraint).isEqualTo("MissingPermission")
        assertThat(e.errors[0].params).contains("permission", "PURGE")
    }

    @Test
    fun `should allow delete an organization`() = test(firstUser) {
        val organization = organizationMutations.organization(
            OrganizationInput(null, "For deletion")
        )
        assertThat(organization.permissions).hasSize(3)

        organizationMutations.deleteOrganization(organization.id)
        assertThat(organizationQueries.organization(organization.id)).isNull()
    }
}
