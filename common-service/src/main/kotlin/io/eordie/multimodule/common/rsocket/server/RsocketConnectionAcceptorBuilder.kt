package io.eordie.multimodule.common.rsocket.server

import io.eordie.multimodule.common.rsocket.client.getServiceInterface
import io.eordie.multimodule.common.utils.like
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.Subscription
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
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

@OptIn(ExperimentalMetadataApi::class)
class RsocketConnectionAcceptorBuilder(
    beanLocator: BeanLocator,
    tracer: Tracer,
    queries: Collection<Query>,
    mutations: Collection<Mutation>,
    subscriptions: List<Subscription>,
) {

    private val index = buildControllerMethods(queries, mutations, subscriptions).associateBy { it.name }.toMutableMap()
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

    private fun buildControllerMethods(
        queries: Collection<Query>,
        mutations: Collection<Mutation>,
        subscriptions: List<Subscription>
    ): List<ControllerDescriptor> {
        return buildControllerMethods(queries, Query::class) +
            buildControllerMethods(mutations, Mutation::class) +
            buildControllerMethods(subscriptions, Subscription::class)
    }

    private fun buildControllerMethods(controllers: Collection<Any>, type: KClass<*>): List<ControllerDescriptor> {
        return controllers
            .mapNotNull { controller ->
                controller::class.getServiceInterface(type)?.let { controller to it }
            }
            .flatMap { (controller, serviceInterface) ->
                val implFunctions = controller::class.declaredFunctions
                serviceInterface.declaredFunctions
                    .filter { it.visibility == KVisibility.PUBLIC }
                    .map { interfaceFunction ->
                        val implementationFunction = implFunctions.like(interfaceFunction) ?: interfaceFunction
                        ControllerDescriptor(controller, serviceInterface, implementationFunction, interfaceFunction)
                    }
            }
    }
}
