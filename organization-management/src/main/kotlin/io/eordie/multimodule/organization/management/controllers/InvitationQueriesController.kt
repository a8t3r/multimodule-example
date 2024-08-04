package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.models.invitation.Invitation
import io.eordie.multimodule.contracts.organization.models.invitation.InvitationFilter
import io.eordie.multimodule.contracts.organization.services.InvitationQueries
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.orDefault
import io.eordie.multimodule.organization.management.repository.InvitationFactory
import jakarta.inject.Singleton

@Singleton
class InvitationQueriesController(
    private val invitations: InvitationFactory
) : InvitationQueries {

    override suspend fun invitation(invitationId: UuidStr): Invitation? {
        return invitations.findById(invitationId)?.convert()
    }

    override suspend fun invitations(filter: InvitationFilter?, pageable: Pageable?): Page<Invitation> {
        return invitations.query(filter.orDefault(), pageable)
    }
}
