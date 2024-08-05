package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.security.context.withSystemContext
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.organization.models.invitation.InvitationFilter
import io.eordie.multimodule.contracts.organization.models.invitation.InvitationStatus
import io.eordie.multimodule.organization.management.models.InvitationModelDraft
import io.eordie.multimodule.organization.management.models.KeycloakEvent
import io.eordie.multimodule.organization.management.repository.InvitationFactory
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post

@Controller("/events")
class KeycloakWebhookController(
    private val invitations: InvitationFactory
) {

    @Post("/wildcard")
    suspend fun applyEvent(@Body event: KeycloakEvent) {
        if (event.isRegistration() && event.details != null) {
            withSystemContext {
                invitations.findAllByFilter(InvitationFilter(email = StringLiteralFilter(eq = event.details.username)))
                    .collect { invitation ->
                        invitations.updateIf<InvitationModelDraft>(invitation.id) {
                            val previousStatus = this.status
                            this.status = InvitationStatus.PENDING
                            this.userId = event.details.userId
                            previousStatus == InvitationStatus.CREATED
                        }
                    }
            }
        }
    }
}
