package io.eordie.multimodule.common.rsocket.client.route

import io.eordie.multimodule.common.rsocket.client.rsocket.RSocketLocalFactory
import io.eordie.multimodule.common.rsocket.context.AclContextElement
import io.eordie.multimodule.common.rsocket.context.AuthenticationContextElement
import io.eordie.multimodule.common.rsocket.meta.AclMetadata
import io.eordie.multimodule.common.rsocket.meta.AuthenticationMetadata
import io.eordie.multimodule.common.rsocket.meta.ExceptionalMetadata
import io.eordie.multimodule.common.rsocket.meta.OpenTelemetrySpanContextMetadata
import io.eordie.multimodule.common.rsocket.meta.ProtobufPayloadBuilder
import io.eordie.multimodule.common.utils.extendWith
import io.eordie.multimodule.contracts.basic.ModuleDefinition
import io.micronaut.context.BeanLocator
import io.opentelemetry.extension.kotlin.getOpenTelemetryContext
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.metadata.CompositeMetadata
import io.rsocket.kotlin.metadata.RoutingMetadata
import io.rsocket.kotlin.metadata.metadata
import io.rsocket.kotlin.metadata.read
import io.rsocket.kotlin.payload.Payload
import io.rsocket.kotlin.payload.buildPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.reflect.Method
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

open class RemoteRoute(
    kotlinIFace: KClass<*>,
    beanLocator: BeanLocator,
    private val rsocketFactory: RSocketLocalFactory
) : SuspendInvoker(kotlinIFace, beanLocator) {

    override val isRemote: Boolean = true

    companion object {
        private val proto = ProtobufPayloadBuilder()
        private val modules = ServiceLoader.load(ModuleDefinition::class.java)
            .associateBy { it.javaClass.`package`.name }
    }

    private fun Method.kotlin() = requireNotNull(this.kotlinFunction)

    @OptIn(ExperimentalMetadataApi::class)
    override suspend fun invoke(method: Method, context: CoroutineContext, arguments: Array<Any?>): Any? {
        val routeId = buildRouteId(method)
        val authentication = context[AuthenticationContextElement]?.details
        val previousAcl = context[AclContextElement.Key]

        val span = tracer.spanBuilder("(Client) $routeId")
            .setParent(context.getOpenTelemetryContext())
            .extendWith(context)
            .setAttribute("Local", false)
            .startSpan()

        val requestPayload = buildPayload {
            proto.encodeToBuilder(arguments.toList(), getGenericParameterTypes(method.kotlin()))(this)

            metadata(
                CompositeMetadata(
                    listOfNotNull(
                        RoutingMetadata(routeId),
                        authentication?.let { AuthenticationMetadata(it) },
                        OpenTelemetrySpanContextMetadata(span.spanContext),
                        previousAcl?.takeIf { it.isInitialized() }?.let { AclMetadata(it.resource) }
                    )
                )
            )
        }

        return try {
            if (method.name.startsWith("broadcast")) {
                rsocketFactory.rsockets(getModuleDefinition())
                    .forEach { invokeMethod(method, requestPayload.copy()) { it } }
            } else {
                val (value, acl) = invokeMethod(method, requestPayload) {
                    rsocketFactory.rsocket(getModuleDefinition())
                }
                previousAcl?.combine(acl)
                value
            }
        } finally {
            span.end()
        }
    }

    private fun getModuleDefinition(): ModuleDefinition {
        val packageName = getServiceDescriptor().name
        return modules.firstNotNullOfOrNull { (key, value) ->
            value?.takeIf { packageName.startsWith(key) }
        } ?: error("specify ModuleDefinition for package")
    }

    protected open fun getReturnType(method: Method) = requireNotNull(method.kotlinFunction).returnType

    protected open fun getGenericParameterTypes(function: KFunction<*>): List<KType> =
        function.valueParameters.map { it.type }

    protected open fun getServiceDescriptor(): Package = kotlinIFace.java.`package`
    protected open fun buildServiceName(): String = requireNotNull(kotlinIFace.simpleName)

    private fun buildRouteId(method: Method): String {
        return buildString {
            append(buildServiceName())
            append(':')
            append(method.kotlinFunction?.name)
            append(method.kotlinFunction?.parameters?.size)
        }
    }

    private suspend fun invokeMethod(
        method: Method,
        requestPayload: Payload,
        rSocketFactory: suspend () -> RSocket
    ): Pair<Any?, AclContextElement?> {
        val rSocket = rSocketFactory()
        val returnType: KType = getReturnType(method)
        return if (returnType.jvmErasure.isSubclassOf(Flow::class)) {
            val responseFlow = rSocket.requestStream(requestPayload)
            val elementType = requireNotNull(returnType.arguments[0].type)
            responseFlow.map {
                produceResponse(it, elementType).first
            } to null
        } else {
            val response = rSocket.requestResponse(requestPayload)
            produceResponse(response, returnType)
        }
    }

    @OptIn(ExperimentalMetadataApi::class)
    private fun produceResponse(response: Payload, returnType: KType): Pair<Any?, AclContextElement?> {
        val entries = response.metadata?.read(CompositeMetadata)?.entries.orEmpty()

        val aclContextElement = entries
            .map { it.mimeType to it.content }
            .firstNotNullOfOrNull { (mimeType, content) ->
                when (mimeType) {
                    ExceptionalMetadata.mimeType -> throw content.read(ExceptionalMetadata).ex
                    AclMetadata.mimeType -> AclContextElement().apply { initialize(content.read(AclMetadata).acl) }
                    else -> null
                }
            }

        val value = proto.decodeFromPayload(response, returnType)
        return value to aclContextElement
    }
}
