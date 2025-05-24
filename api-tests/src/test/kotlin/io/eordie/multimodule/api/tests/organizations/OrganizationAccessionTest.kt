package io.eordie.multimodule.api.tests.organizations

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.eordie.multimodule.api.tests.AuthUtils.authWith
import io.eordie.multimodule.contracts.basic.exception.ValidationException
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.organization.OrganizationInput
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequestInput
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequestStatus
import io.eordie.multimodule.contracts.organization.services.AccessionRequestMutations
import io.eordie.multimodule.contracts.organization.services.AccessionRequestQueries
import io.eordie.multimodule.contracts.utils.Roles.MANAGE_INVITATIONS
import io.eordie.multimodule.contracts.utils.Roles.VIEW_INVITATIONS
import jakarta.inject.Inject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrganizationAccessionTest : AbstractOrganizationTest() {

    @Inject
    lateinit var accessionQueries: AccessionRequestQueries

    @Inject
    lateinit var accessionMutations: AccessionRequestMutations

    private val foreignUser = authWith(firstOrg, developer1)

    lateinit var organization: Organization

    @BeforeEach
    fun setUp() {
        test(foreignUser) {
            accessionQueries.accessionRequests().data.forEach {
                accessionMutations.deleteJoinRequest(it.id)
            }
        }

        test(authWith(secondOrg, developer2)) {
            organization = organizationMutations.organization(
                OrganizationInput(null, "Accession organization")
            )
        }
    }

    @Test
    fun `should create accession request to organization and reject it`() {
        test(foreignUser) {
            val request = accessionMutations.accessionRequest(AccessionRequestInput(null, organization.id.toString()))
            assertThat(request.id).isNotNull()
            assertThat(request.status).isEqualTo(AccessionRequestStatus.CREATED)
        }

        val currentOrganization = CurrentOrganization(organization.id)
        val organizationUser = authWith(currentOrganization, developer2, VIEW_INVITATIONS, MANAGE_INVITATIONS)
        test(organizationUser) {
            val requests = accessionQueries.accessionRequests().data
            assertThat(requests).isNotEmpty()
            assertThat(requests).hasSize(1)

            assertThat(requests[0].status).isEqualTo(AccessionRequestStatus.CREATED)
            assertThat(requests[0].initiatedBy).isNotNull()
            assertThat(requests[0].initiatedBy.id).isEqualTo(foreignUser.details.userId)
            assertThat(requests[0].processedBy).isNull()

            val expected = accessionMutations.rejectAccessionRequest(requests[0].id, "foobar")
            assertThat(expected.status).isEqualTo(AccessionRequestStatus.REJECTED)
            assertThat(expected.rejectionMessage).isEqualTo("foobar")
            assertThat(expected.processedBy).isNotNull()
            assertThat(expected.processedBy?.id).isEqualTo(developer2)
        }

        test(foreignUser) {
            val requests = accessionQueries.accessionRequests().data
            assertThat(requests).isNotEmpty()
            assertThat(requests).hasSize(1)

            assertThat(requests[0].status).isEqualTo(AccessionRequestStatus.REJECTED)
            assertThat(requests[0].rejectionMessage).isEqualTo("foobar")
            assertThat(requests[0].processedBy).isNotNull()

            val e = assertThrows<ValidationException> {
                accessionMutations.accessionRequest(AccessionRequestInput(requests[0].id, requests[0].vat))
            }
            assertThat(e.errors).hasSize(1)
            assertThat(e.errors[0].constraint).isEqualTo("AccessionRequestFinalState")
        }
    }

    @Test
    fun `should create accession request to organization and accept it`() {
        test(foreignUser) {
            val request = accessionMutations.accessionRequest(AccessionRequestInput(null, organization.id.toString()))
            assertThat(request.id).isNotNull()
            assertThat(request.status).isEqualTo(AccessionRequestStatus.CREATED)
        }

        val currentOrganization = CurrentOrganization(organization.id)
        val organizationUser = authWith(currentOrganization, developer2, VIEW_INVITATIONS, MANAGE_INVITATIONS)
        test(organizationUser) {
            val requests = accessionQueries.accessionRequests().data
            assertThat(requests).isNotEmpty()
            assertThat(requests).hasSize(1)

            assertThat(requests[0].status).isEqualTo(AccessionRequestStatus.CREATED)
            assertThat(requests[0].initiatedBy).isNotNull()
            assertThat(requests[0].initiatedBy.id).isEqualTo(foreignUser.details.userId)
            assertThat(requests[0].processedBy).isNull()

            val expected = accessionMutations.acceptAccessionRequest(requests[0].id)
            assertThat(expected.status).isEqualTo(AccessionRequestStatus.ACCEPTED)
            assertThat(expected.rejectionMessage).isNull()
            assertThat(expected.processedBy).isNotNull()
            assertThat(expected.processedBy?.id).isEqualTo(developer2)
        }

        test(foreignUser) {
            val requests = accessionQueries.accessionRequests().data
            assertThat(requests).isNotEmpty()
            assertThat(requests).hasSize(1)

            assertThat(requests[0].status).isEqualTo(AccessionRequestStatus.ACCEPTED)
            assertThat(requests[0].rejectionMessage).isNull()
            assertThat(requests[0].processedBy).isNotNull()

            val e = assertThrows<ValidationException> {
                accessionMutations.accessionRequest(AccessionRequestInput(requests[0].id, requests[0].vat))
            }
            assertThat(e.errors).hasSize(1)
            assertThat(e.errors[0].constraint).isEqualTo("AccessionRequestFinalState")
        }
    }
}
