package io.eordie.multimodule.common.kafka

import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.messaging.annotation.MessageHeader

interface EventsKafkaClient {
    fun sendEvent(
        @Topic topic: String,
        @KafkaKey key: String,
        @MessageHeader("x-authentication-context")
        auth: AuthenticationDetails,
        payload: String
    )
}
