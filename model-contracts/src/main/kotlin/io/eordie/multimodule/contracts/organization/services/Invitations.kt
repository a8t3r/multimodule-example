package io.eordie.multimodule.contracts.organization.services

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.models.invitation.Invitation
import io.eordie.multimodule.contracts.organization.models.invitation.InvitationFilter
import io.eordie.multimodule.contracts.organization.models.invitation.InvitationInput
import io.eordie.multimodule.contracts.utils.UuidStr

@AutoService(Query::class)
interface InvitationQueries : Query {

    suspend fun invitation(invitationId: UuidStr): Invitation?

    suspend fun invitations(
        filter: InvitationFilter? = null,
        pageable: Pageable? = null
    ): Page<Invitation>
}

@AutoService(Mutation::class)
interface InvitationMutations : Mutation {
    suspend fun invitation(input: InvitationInput): Invitation

    suspend fun deleteInvitation(invitationId: UuidStr): Boolean

    suspend fun acceptInvitation(invitationId: UuidStr)
}
