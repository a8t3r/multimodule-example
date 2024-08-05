package io.eordie.multimodule.common.kafka

import com.google.common.base.CaseFormat
import io.eordie.multimodule.common.repository.event.EventPublisher
import io.eordie.multimodule.common.security.context.getAuthenticationContext
import io.eordie.multimodule.contracts.basic.event.MutationEvent
import io.eordie.multimodule.contracts.utils.JsonModule
import kotlinx.serialization.serializer
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

class KafkaEventPublisher(private val client: EventsKafkaClient) : EventPublisher {

    private val proto = JsonModule.getInstance()

    override fun <T : Any> publish(target: KClass<out T>, affectedBy: CoroutineContext, event: MutationEvent<T>) {
        val topic = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, requireNotNull(target.simpleName))
        val serializer = proto.serializersModule.serializer(
            MutationEvent::class.createType(listOf(KTypeProjection.invariant(target.createType())))
        )

        val payload = proto.encodeToString(serializer, event)
        client.sendEvent(topic, event.id, affectedBy.getAuthenticationContext(), payload)
    }
}
