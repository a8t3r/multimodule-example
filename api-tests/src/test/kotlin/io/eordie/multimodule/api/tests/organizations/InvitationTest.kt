package io.eordie.multimodule.api.tests.organizations

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.eordie.multimodule.api.tests.AuthUtils.authWith
import io.eordie.multimodule.contracts.basic.exception.ValidationException
import io.eordie.multimodule.contracts.organization.models.invitation.Invitation
import io.eordie.multimodule.contracts.organization.models.invitation.InvitationInput
import io.eordie.multimodule.contracts.organization.models.invitation.InvitationStatus
import io.eordie.multimodule.contracts.utils.Roles.MANAGE_INVITATIONS
import io.eordie.multimodule.contracts.utils.Roles.MANAGE_ORGANIZATION
import io.eordie.multimodule.contracts.utils.Roles.VIEW_INVITATIONS
import io.eordie.multimodule.contracts.utils.Roles.VIEW_ORGANIZATION
import io.eordie.multimodule.contracts.utils.asRoleSet
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertThrows

@TestMethodOrder(value = MethodOrderer.OrderAnnotation::class)
class InvitationTest : AbstractOrganizationTest() {

    companion object {
        private val inviter = authWith(firstOrg) {
            copy(
                userId = developer1,
                roleSet = listOf(
                    VIEW_INVITATIONS,
                    MANAGE_INVITATIONS,
                    VIEW_ORGANIZATION,
                    MANAGE_ORGANIZATION
                ).asRoleSet()
            )
        }
        private val invited = authWith(secondOrg) {
            copy(
                userId = developer2,
                email = "developer2@nowhere.com"
            )
        }
    }

    private suspend fun currentInvitation(): Invitation {
        val data = invitationQueries.invitations().data
        assertThat(data).hasSize(1)
        return data[0]
    }

    @Test
    @Order(0)
    fun `should allow modify just created invitation`() = test(inviter) {
        val firstAttempt = invitationMutations.invitation(
            InvitationInput(email = "firstAttempt@nowhere.com")
        )
        assertThat(firstAttempt.id).isNotNull()
        assertThat(firstAttempt.userId).isNull()
        assertThat(firstAttempt.organizationId).isEqualTo(inviter.details.currentOrganizationId)
        assertThat(firstAttempt.status).isEqualTo(InvitationStatus.CREATED)
        assertThat(firstAttempt.email).isEqualTo("firstAttempt@nowhere.com")

        val secondAttempt = invitationMutations.invitation(
            InvitationInput(firstAttempt.id, "secondAttempt@nowhere.com")
        )
        assertThat(secondAttempt.id).isEqualTo(firstAttempt.id)
        assertThat(secondAttempt.userId).isNull()
        assertThat(firstAttempt.organizationId).isEqualTo(inviter.details.currentOrganizationId)
        assertThat(secondAttempt.status).isEqualTo(InvitationStatus.CREATED)
        assertThat(secondAttempt.email).isEqualTo("secondAttempt@nowhere.com")
    }

    @Test
    @Order(5)
    fun `should delete already created invitation`() = test(inviter) {
        val invitation = currentInvitation()
        assertThat(invitation.status).isEqualTo(InvitationStatus.CREATED)
        assertThat(invitationMutations.deleteInvitation(invitation.id)).isTrue()
    }

    @Test
    @Order(10)
    fun `should create invitation`() = test(inviter) {
        val invitation = invitationMutations.invitation(
            InvitationInput(email = invited.details.email)
        )

        assertThat(invitation.id).isNotNull()
        assertThat(invitation.userId).isEqualTo(developer2)
        assertThat(invitation.organizationId).isEqualTo(inviter.details.currentOrganizationId)
        assertThat(invitation.status).isEqualTo(InvitationStatus.PENDING)
        assertThat(invitation.email).isEqualTo(invited.details.email)
    }

    @Test
    @Order(20)
    fun `should fail on pending invitation modification by id`() = test(inviter) {
        val invitation = currentInvitation()
        assertThat(invitation.status).isEqualTo(InvitationStatus.PENDING)

        val e = assertThrows<ValidationException> {
            invitationMutations.invitation(InvitationInput(invitation.id, invited.details.email))
        }
        assertThat(e.errors).hasSize(1)
        assertThat(e.errors[0].constraint).isEqualTo("PendingInvitation")
        assertThat(e.errors[0].message).isEqualTo("Pending invitation couldn't be modified")
    }

    @Test
    @Order(20)
    fun `should fail on pending invitation modification by email`() = test(inviter) {
        val e = assertThrows<ValidationException> {
            invitationMutations.invitation(InvitationInput(email = invited.details.email))
        }
        assertThat(e.errors).hasSize(1)
        assertThat(e.errors[0].constraint).isEqualTo("PendingInvitation")
        assertThat(e.errors[0].message).isEqualTo("Pending invitation couldn't be modified")
    }

    @Test
    @Order(30)
    fun `should accept invitation`() = test(invited) {
        val invitation = currentInvitation()
        assertThat(invitation.id).isNotNull()
        assertThat(invitation.userId).isEqualTo(developer2)
        assertThat(invitation.organizationId).isEqualTo(inviter.details.currentOrganizationId)
        assertThat(invitation.status).isEqualTo(InvitationStatus.PENDING)
        assertThat(invitation.email).isEqualTo(invited.details.email)

        invitationMutations.acceptInvitation(invitation.id)

        val actual = invitationQueries.invitation(invitation.id)
        assertThat(actual).isNotNull()
        assertThat(actual?.userId).isEqualTo(developer2)
        assertThat(actual?.status).isEqualTo(InvitationStatus.ACCEPTED)
    }

    @Test
    @Order(40)
    fun `should fail on already employed user`() = test(inviter) {
        val e = assertThrows<ValidationException> {
            invitationMutations.invitation(InvitationInput(email = invited.details.email))
        }
        assertThat(e.errors).hasSize(1)
        assertThat(e.errors[0].constraint).isEqualTo("UserAlreadyEmployed")
        assertThat(e.errors[0].message).isEqualTo("User already employed to current organization")
    }

    @Test
    @Order(50)
    fun `should not accept invitation at second attempt`() = test(invited) {
        val invitation = currentInvitation()
        val e = assertThrows<ValidationException> {
            invitationMutations.acceptInvitation(invitation.id)
        }
        assertThat(e.errors).hasSize(1)
        assertThat(e.errors[0].constraint).isEqualTo("UserAlreadyEmployed")
    }

    @Test
    @Order(60)
    fun `should delete invitation`() = test(inviter) {
        val invitation = currentInvitation()
        assertThat(invitation.status).isEqualTo(InvitationStatus.ACCEPTED)
        assertThat(invitationMutations.deleteInvitation(invitation.id)).isTrue()
        assertThat(invitationQueries.invitation(invitation.id)).isNull()
    }
}
