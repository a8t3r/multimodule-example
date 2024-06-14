package io.eordie.multimodule.common.repository.event

import io.eordie.multimodule.contracts.basic.event.MutationEvent
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class BlockingEventPublisher(
    private val index: Map<Class<*>, Collection<EventListener<Any>>>
) : EventPublisher {

    override fun <T : Any> publish(target: KClass<out T>, affectedBy: CoroutineContext, event: MutationEvent<T>) {
        runBlocking {
            withContext(affectedBy) {
                index[target.java]?.forEach { listener ->
                    listener.onEvent(event as MutationEvent<Any>)
                }
            }
        }
    }
}
