package io.eordie.multimodule.common.kafka

import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Singleton
@Replaces(EventsKafkaClient::class)
@Requires(property = "kafka.enabled", notEquals = "true")
class EventsKafkaClientFallback : EventsKafkaClient {
    override fun sendEvent(topic: String, key: String, auth: AuthenticationDetails, payload: String) {
        error("unreachable state")
    }
}
