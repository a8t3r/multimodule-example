package io.eordie.multimodule.example.rsocket.server

import io.eordie.multimodule.example.rsocket.client.getServiceInterface
import io.micronaut.context.BeanLocator
import io.opentelemetry.api.trace.Tracer
import io.rsocket.kotlin.ConnectionAcceptor
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.metadata.CompositeMetadata
import io.rsocket.kotlin.metadata.RoutingMetadata
import io.rsocket.kotlin.metadata.read
import io.rsocket.kotlin.payload.Payload
import kotlinx.coroutines.withContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

@OptIn(ExperimentalMetadataApi::class)
class RsocketConnectionAcceptorBuilder(
    beanLocator: BeanLocator,
    tracer: Tracer,
    controllers: Collection<Any>
) {

    private val index = buildControllerMethods(controllers).associateBy { it.name }.toMutableMap()
    private val invocationHelper = InvocationHelper(beanLocator, tracer)

    fun createAcceptor(): ConnectionAcceptor {
        return ConnectionAcceptor {
            RSocketRequestHandler {
                requestResponse { payload ->
                    withContext(EmptyCoroutineContext) {
                        val (entries, controllerDescriptor) = getDescriptor(payload)
                        invocationHelper.prepareAndPerformInvocation(controllerDescriptor, payload, entries)
                    }
                }

                requestStream { payload ->
                    val (entries, controllerDescriptor) = getDescriptor(payload)
                    invocationHelper.prepareAndPerformInvocationFlow(controllerDescriptor, payload, entries)
                }
            }
        }
    }

    private fun getDescriptor(payload: Payload): Pair<List<CompositeMetadata.Entry>, ControllerDescriptor> {
        val entries = payload.metadata?.read(CompositeMetadata)?.entries.orEmpty()
        val route = entries.firstOrNull { it.mimeType == RoutingMetadata.mimeType }
            ?.content?.read(RoutingMetadata)?.tags?.firstOrNull()
            ?: error("No route provided")

        val controllerDescriptor = index[route] ?: error("Wrong route: $route")
        return Pair(entries, controllerDescriptor)
    }

    fun addDescriptors(descriptors: List<ControllerDescriptor>): RsocketConnectionAcceptorBuilder {
        descriptors.forEach { addDescriptor(it) }
        return this
    }

    private fun addDescriptor(descriptor: ControllerDescriptor): RsocketConnectionAcceptorBuilder {
        index[descriptor.name] = descriptor
        return this
    }

    private fun buildControllerMethods(controllers: Collection<Any>): List<ControllerDescriptor> {
        return controllers
            .flatMap { controller ->
                controller::class.declaredFunctions
                    .filter { it.visibility == KVisibility.PUBLIC }
                    .map { controller to it }
            }
            .mapNotNull { (controller, function) ->
                controller::class.getServiceInterface()?.let {
                    ControllerDescriptor(controller, it, function)
                }
            }
    }
}
