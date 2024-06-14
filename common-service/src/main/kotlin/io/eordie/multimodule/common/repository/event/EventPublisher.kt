package io.eordie.multimodule.common.repository.event

import io.eordie.multimodule.contracts.basic.event.MutationEvent
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

interface EventPublisher {
    fun <T : Any> publish(target: KClass<out T>, affectedBy: CoroutineContext, event: MutationEvent<T>)
}
