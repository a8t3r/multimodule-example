package io.eordie.multimodule.common.repository.event

import io.eordie.multimodule.contracts.basic.event.MutationEvent
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.micronaut.messaging.annotation.MessageHeader

interface EventListener<T> {
    suspend fun onEvent(
        @MessageHeader("x-authentication-context")
        causedBy: AuthenticationDetails?,
        event: MutationEvent<T>
    )
}
