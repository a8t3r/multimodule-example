package io.eordie.multimodule.example.rsocket.client.invocation

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.eordie.multimodule.example.contracts.basic.exception.UnexpectedInvocationException
import io.eordie.multimodule.example.rsocket.context.AuthenticationContextElement
import io.eordie.multimodule.example.rsocket.meta.AuthenticationMetadata
import io.eordie.multimodule.example.rsocket.meta.ExceptionalMetadata
import io.eordie.multimodule.example.rsocket.meta.OpenTelemetrySpanContextMetadata
import io.eordie.multimodule.example.rsocket.meta.ProtobufPayloadBuilder
import io.eordie.multimodule.example.utils.extendWith
import io.micronaut.context.BeanLocator
import io.micronaut.discovery.consul.client.v1.ConsulClient
import io.micronaut.discovery.consul.client.v1.ConsulServiceEntry
import io.opentelemetry.extension.kotlin.getOpenTelemetryContext
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.core.RSocketConnector
import io.rsocket.kotlin.metadata.CompositeMetadata
import io.rsocket.kotlin.metadata.RoutingMetadata
import io.rsocket.kotlin.metadata.metadata
import io.rsocket.kotlin.metadata.read
import io.rsocket.kotlin.payload.Payload
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.transport.ktor.tcp.TcpClientTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import reactor.core.publisher.Mono
import java.lang.reflect.Method
import java.time.Duration
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

class RemoteRoute(
    kotlinIFace: KClass<*>,
    beanLocator: BeanLocator,
    private val routePrefix: String = ""
) : SuspendInvoker(kotlinIFace, beanLocator) {

    override val isRemote: Boolean = true

    private val discoveryClient: ConsulClient by lazy {
        beanLocator.getBean(ConsulClient::class.java)
    }

    companion object {
        private val proto = ProtobufPayloadBuilder()

        private const val EXPIRATION_MINUTES = 10L

        private val rsocketCache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(EXPIRATION_MINUTES))
            .build(object : CacheLoader<Pair<String, Int>, Deferred<RSocket>>() {
                override fun load(key: Pair<String, Int>): Deferred<RSocket> {
                    val transport = TcpClientTransport(key.first, key.second)
                    val connector = RSocketConnector {}
                    return CoroutineScope(Dispatchers.IO).async { connector.connect(transport) }
                }
            })
    }

    private fun consulServiceEntry(
        discoveryClient: ConsulClient,
        serviceInterface: Class<out Any>
    ): ConsulServiceEntry {
        val future = Mono.from(discoveryClient.findServices())
            .map { services ->
                services.entries.filter { (_, entry) ->
                    entry.tags.any { it == "[${serviceInterface.simpleName}]=1" }
                }.map { it.value }
            }
            .mapNotNull { entries -> entries.takeIf { it.isNotEmpty() }?.random() }
            .toFuture()

        return future.join() ?: error("no service implementation [${serviceInterface.simpleName}] found")
    }

    private suspend fun rSocketFactory(): RSocket {
        val entry = consulServiceEntry(discoveryClient, kotlinIFace.java)
        val rsocketPort = entry.tags.first { it.startsWith("rsocket_port=") }
            .substringAfter("=").toInt()

        val socketPair = entry.address to rsocketPort
        return rsocketCache.get(socketPair).await().takeIf { it.isActive }
            ?: run {
                rsocketCache.refresh(socketPair)
                rsocketCache.get(socketPair).await()
            }
    }

    override suspend fun invoke(method: Method, context: CoroutineContext, arguments: Array<Any?>): Any? {
        return invoke(routePrefix, method, context, arguments)
    }

    @OptIn(ExperimentalMetadataApi::class)
    suspend fun invoke(routePrefix: String, method: Method, context: CoroutineContext, arguments: Array<Any?>): Any? {
        val routeId = "$routePrefix${kotlinIFace.simpleName}:${method.name}${method.kotlinFunction?.parameters?.size}"
        val authentication = context[AuthenticationContextElement]?.details

        val span = tracer.spanBuilder("(Client) $routeId")
            .setParent(context.getOpenTelemetryContext())
            .extendWith(context)
            .setAttribute("Local", false)
            .startSpan()

        val requestPayload = buildPayload {
            proto.encodeToBuilder(arguments.toList(), method.genericParameterTypes.toList())(this)

            metadata(
                CompositeMetadata(
                    listOfNotNull(
                        RoutingMetadata(routeId),
                        authentication?.let { AuthenticationMetadata(it) },
                        OpenTelemetrySpanContextMetadata(span.spanContext)
                    )
                )
            )
        }

        return try {
            invokeMethod(method, requestPayload) { rSocketFactory() }
        } finally {
            span.end()
        }
    }

    private suspend fun invokeMethod(
        method: Method,
        requestPayload: Payload,
        rSocketFactory: suspend () -> RSocket
    ): Any? {
        val rSocket = rSocketFactory()
        val returnType: KType = requireNotNull(method.kotlinFunction).returnType
        return if (returnType.jvmErasure.isSubclassOf(Flow::class)) {
            val responseFlow = rSocket.requestStream(requestPayload)
            val elementType = requireNotNull(returnType.arguments[0].type)
            responseFlow.map {
                produceResponse(it, elementType)
            }
        } else {
            val response = rSocket.requestResponse(requestPayload)
            produceResponse(response, returnType)
        }
    }

    @OptIn(ExperimentalMetadataApi::class)
    private fun produceResponse(response: Payload, returnType: KType): Any? {
        val metadata = response.metadata?.read(CompositeMetadata)
        // exception handling / tracing / logging / etc
        if (metadata != null) {
            val exceptional = metadata.entries
                .filter { it.mimeType == ExceptionalMetadata.mimeType }
                .map { it.content.read(ExceptionalMetadata) }
                .firstOrNull()

            if (exceptional != null) {
                throw UnexpectedInvocationException(exceptional.definition)
            }
        }

        return proto.decodeFromPayload(response, returnType)
    }
}
