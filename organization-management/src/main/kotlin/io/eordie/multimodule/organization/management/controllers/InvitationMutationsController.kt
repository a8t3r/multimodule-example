package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.security.context.getAuthentication
import io.eordie.multimodule.common.security.context.withSystemContext
import io.eordie.multimodule.common.validation.error
import io.eordie.multimodule.contracts.organization.models.invitation.Invitation
import io.eordie.multimodule.contracts.organization.models.invitation.InvitationInput
import io.eordie.multimodule.contracts.organization.models.invitation.InvitationStatus
import io.eordie.multimodule.contracts.organization.services.InvitationMutations
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.organization.management.models.InvitationModelDraft
import io.eordie.multimodule.organization.management.models.OrganizationEmployeeModelDraft
import io.eordie.multimodule.organization.management.models.UserModel
import io.eordie.multimodule.organization.management.models.by
import io.eordie.multimodule.organization.management.models.email
import io.eordie.multimodule.organization.management.models.organizationId
import io.eordie.multimodule.organization.management.repository.InvitationFactory
import io.eordie.multimodule.organization.management.repository.OrganizationEmployeeFactory
import io.eordie.multimodule.organization.management.repository.OrganizationMemberRepository
import io.eordie.multimodule.organization.management.repository.UserFactory
import io.eordie.multimodule.organization.management.validation.PendingInvitation
import io.eordie.multimodule.organization.management.validation.UserAlreadyEmployed
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher

@Singleton
class InvitationMutationsController(
    private val users: UserFactory,
    private val invitations: InvitationFactory,
    private val membership: OrganizationMemberRepository,
    private val employees: OrganizationEmployeeFactory
) : InvitationMutations {

    private suspend fun getInvitedUser(email: String): UserModel? {
        val fetcher = newFetcher(UserModel::class).by {
            allScalarFields()
            membership {
                organizationId()
            }
        }
        return withSystemContext {
            users.findOneBySpecification(fetcher) {
                where(table.email eq email)
            }
        }
    }

    override suspend fun invitation(input: InvitationInput): Invitation {
        val currentOrganizationId = requireNotNull(getAuthentication().currentOrganizationId)

        val invited = getInvitedUser(input.email)
        if (invited != null && invited.organizationIds().contains(currentOrganizationId)) {
            UserAlreadyEmployed.error()
        }

        return invitations.save<InvitationModelDraft>(input.id) { isNew, instance ->
            if (isNew) {
                instance.status = if (invited == null) InvitationStatus.CREATED else InvitationStatus.PENDING
            } else if (instance.status != InvitationStatus.CREATED) {
                PendingInvitation.error()
            }

            instance.userId = invited?.id
            instance.email = input.email
            instance.departmentId = input.departmentId
            instance.positionId = input.positionId
        }.convert()
    }

    override suspend fun deleteInvitation(invitationId: UuidStr): Boolean {
        return invitations.deleteById(invitationId)
    }

    override suspend fun acceptInvitation(invitationId: UuidStr) {
        val auth = getAuthentication()
        val invitation = invitations.getById(invitationId)
        if (invitation.userId == auth.userId &&
            invitation.organizationId !in auth.organizationIds() &&
            invitation.status == InvitationStatus.ACCEPTED
        ) {
            UserAlreadyEmployed.error()
        }

        withSystemContext {
            invitations.save<InvitationModelDraft>(invitation.id) { _, instance ->
                instance.status = InvitationStatus.ACCEPTED
            }

            val membershipId = membership.addMemberToOrganization(
                auth.userId,
                invitation.organizationId
            )

            if (invitation.positionId != null) {
                employees.save<OrganizationEmployeeModelDraft>(null) { _, value ->
                    value.memberId = membershipId
                    value.createdBy = invitation.createdBy
                    value.userId = requireNotNull(invitation.userId)
                    value.organizationId = invitation.organizationId
                    value.departmentId = invitation.departmentId
                    value.positionId = invitation.positionId
                }
            }
        }
    }
}
