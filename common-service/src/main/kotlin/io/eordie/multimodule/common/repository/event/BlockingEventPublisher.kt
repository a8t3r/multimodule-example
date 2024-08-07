package io.eordie.multimodule.common.repository.event

import io.eordie.multimodule.contracts.basic.event.MutationEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class BlockingEventPublisher(
    private val index: Map<Class<*>, Collection<EventListener<Any>>>
) : EventPublisher {

    private val logger = KotlinLogging.logger {}

    override fun <T : Any> publish(target: KClass<out T>, affectedBy: CoroutineContext, event: MutationEvent<T>) {
        runBlocking {
            withContext(affectedBy) {
                index[target.java]?.forEach { listener ->
                    logger.info { "Processing event of type ${target.simpleName} by ${listener.javaClass.simpleName}" }
                    listener.onEvent(event as MutationEvent<Any>)
                }
            }
        }
    }
}
