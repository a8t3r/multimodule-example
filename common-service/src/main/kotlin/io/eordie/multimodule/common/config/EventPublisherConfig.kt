package io.eordie.multimodule.common.config

import com.google.common.collect.HashMultimap
import io.eordie.multimodule.common.kafka.EventsKafkaClient
import io.eordie.multimodule.common.kafka.KafkaEventPublisher
import io.eordie.multimodule.common.repository.event.BlockingEventPublisher
import io.eordie.multimodule.common.repository.event.EventListener
import io.eordie.multimodule.common.repository.event.EventPublisher
import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.core.reflect.GenericTypeUtils.resolveInterfaceTypeArgument
import kotlin.jvm.optionals.getOrElse

@Factory
class EventPublisherConfig {

    @Bean
    @Requires(env = [ "test" ])
    fun blockingEventPublisher(
        listeners: List<EventListener<Any>>
    ): EventPublisher {
        val index = listeners.fold(HashMultimap.create<Class<*>, EventListener<Any>>()) { acc, listener ->
            val type = resolveInterfaceTypeArgument(listener::class.java, EventListener::class.java)
                .getOrElse { error("couldn't resolve type argument for class ${listener.javaClass}") }
            acc.put(type, listener)
            acc
        }.asMap()

        return BlockingEventPublisher(index)
    }

    @Bean
    @Requires(notEnv = [ "test" ])
    fun kafkaEventPublisher(client: EventsKafkaClient): EventPublisher {
        return KafkaEventPublisher(client)
    }

    @KafkaClient
    @Requires(property = "kafka.enabled", value = "true")
    interface EventsKafkaClientDeclaration : EventsKafkaClient
}
