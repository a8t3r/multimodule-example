package io.eordie.multimodule.common.rsocket.server

import io.eordie.multimodule.common.rsocket.client.route.AuthorizationCheck
import io.eordie.multimodule.common.rsocket.client.route.ValidationCheck
import io.eordie.multimodule.common.rsocket.client.route.ValidationCheck.toErrors
import io.eordie.multimodule.common.rsocket.meta.AclMetadata
import io.eordie.multimodule.common.rsocket.meta.AuthenticationMetadata
import io.eordie.multimodule.common.rsocket.meta.ExceptionalMetadata
import io.eordie.multimodule.common.rsocket.meta.OpenTelemetrySpanContextMetadata
import io.eordie.multimodule.common.rsocket.meta.ProtobufPayloadBuilder
import io.eordie.multimodule.common.rsocket.meta.SelectionSetMetadata
import io.eordie.multimodule.common.security.context.AclContextElement
import io.eordie.multimodule.common.security.context.AuthenticationContextElement
import io.eordie.multimodule.common.security.context.SelectionSetContextElement
import io.eordie.multimodule.common.utils.extendWith
import io.eordie.multimodule.contracts.annotations.Secured
import io.eordie.multimodule.contracts.annotations.Valid
import io.eordie.multimodule.contracts.basic.exception.BaseRuntimeException
import io.eordie.multimodule.contracts.basic.exception.ExceptionDefinition
import io.eordie.multimodule.contracts.basic.exception.UnexpectedInvocationException
import io.eordie.multimodule.contracts.basic.exception.ValidationException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.core.*
import io.micronaut.context.BeanLocator
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.extension.kotlin.getOpenTelemetryContext
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.metadata.CompositeMetadata
import io.rsocket.kotlin.metadata.compositeMetadata
import io.rsocket.kotlin.metadata.read
import io.rsocket.kotlin.payload.Payload
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.valiktor.ConstraintViolationException
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

@OptIn(ExperimentalMetadataApi::class)
class InvocationHelper(private val beanLocator: BeanLocator, private val tracer: Tracer) {

    companion object {
        private val proto = ProtobufPayloadBuilder()
        private val logger = KotlinLogging.logger { }
    }

    suspend fun prepareAndPerformInvocation(
        descriptor: ControllerDescriptor,
        payload: Payload,
        entries: List<CompositeMetadata.Entry>
    ): Payload {
        val (returnValue, aclContext) = invoke(entries, descriptor, payload)

        return when {
            returnValue is BaseRuntimeException -> buildExceptionPayload(returnValue)
            else -> buildPayload {
                data {
                    proto.encodeToPacketBuilder(returnValue, descriptor.implementationFunction.returnType)(this)
                }
                compositeMetadata {
                    aclContext?.let { add(AclMetadata(it.resource)) }
                }
            }
        }
    }

    suspend fun prepareAndPerformInvocationFlow(
        descriptor: ControllerDescriptor,
        payload: Payload,
        entries: List<CompositeMetadata.Entry>
    ): Flow<Payload> {
        val context = prepareContext(entries)
        val arguments = prepareInvocationArguments(context, descriptor, payload)
        val returnValue = performInvocation(descriptor, context, arguments)
        val targetType = descriptor.implementationFunction.returnType

        return when {
            returnValue is BaseRuntimeException -> flowOf(buildExceptionPayload(returnValue))
            else -> {
                check(targetType.jvmErasure.isSubclassOf(Flow::class))
                val elementType = requireNotNull(targetType.arguments[0].type)
                (returnValue as Flow<*>)
                    .flowOn(context)
                    .map { proto.encodeToPayload(it, elementType) }
            }
        }
    }

    private fun buildExceptionPayload(ex: BaseRuntimeException) = buildPayload {
        data { writeInt(0) }
        compositeMetadata {
            add(ExceptionalMetadata(ex))
        }
    }

    private suspend fun invoke(
        entries: List<CompositeMetadata.Entry>,
        descriptor: ControllerDescriptor,
        payload: Payload
    ): Pair<Any?, AclContextElement?> {
        val context = prepareContext(entries)
        val element = context[AclContextElement.Key]
        val notInitialized = element?.isInitialized() != true

        val arguments = prepareInvocationArguments(context, descriptor, payload)
        val result = performInvocation(descriptor, context, arguments)
        return result to (element?.takeIf { notInitialized && it.isInitialized() })
    }

    private suspend fun performInvocation(
        descriptor: ControllerDescriptor,
        context: CoroutineContext,
        arguments: Map<KParameter, Any?>
    ): Any? {
        beforeInvocation(descriptor, context, arguments)?.let { return it }

        val span = tracer.spanBuilder("(Server) ${descriptor.name}")
            .setParent(context.getOpenTelemetryContext())
            .extendWith(context)
            .startSpan()

        return try {
            withContext(context + span.asContextElement()) {
                descriptor.implementationFunction.callSuspendBy(arguments)
            }
        } catch (ex: Exception) {
            val reason = ex.cause ?: ex
            span.recordException(reason)
            when (reason) {
                is BaseRuntimeException -> reason
                is ConstraintViolationException -> ValidationException(reason.toErrors(context))
                else -> {
                    logger.error(ex) { "unexpected exception" }
                    UnexpectedInvocationException(ExceptionDefinition(reason))
                }
            }
        } finally {
            span.end()
        }
    }

    private suspend fun beforeInvocation(
        descriptor: ControllerDescriptor,
        context: CoroutineContext,
        arguments: Map<KParameter, Any?>
    ): BaseRuntimeException? {
        val argumentsIndex = arguments.mapKeys { it.key.name }

        val exception = AuthorizationCheck.check(
            descriptor.serviceInterface.findAnnotations<Secured>().firstOrNull(),
            descriptor.securedConstraints,
            context[AuthenticationContextElement]?.details
        ) ?: ValidationCheck.check(
            context,
            beanLocator,
            descriptor.serviceFunction.valueParameters
                .filter { it.findAnnotations(Valid::class).isNotEmpty() }
                .mapNotNull { param -> argumentsIndex[param.name]?.let { param to it } }
        )

        return exception
    }

    private fun prepareInvocationArguments(
        context: CoroutineContext,
        descriptor: ControllerDescriptor,
        payload: Payload
    ): Map<KParameter, Any?> {
        val valueParameters = descriptor.implementationFunction.valueParameters
        val valueArguments = if (valueParameters.isEmpty()) emptyMap() else {
            val arguments = proto.decodeFromPayload(payload, valueParameters.map { it.type }, context)
            valueParameters.zip(arguments).associateBy({ it.first }, { it.second })
        }
        return valueArguments.plus(
            requireNotNull(descriptor.implementationFunction.instanceParameter) to descriptor.implementation
        )
    }

    private fun prepareContext(entries: List<CompositeMetadata.Entry>): CoroutineContext {
        return entries
            .map { it.mimeType to it.content }
            .mapNotNull { (mimeType, content) ->
                when (mimeType) {
                    AclMetadata.mimeType -> AclContextElement().apply {
                        this.resource = content.read(AclMetadata).acl
                    }

                    AuthenticationMetadata.mimeType ->
                        AuthenticationContextElement(content.read(AuthenticationMetadata).details)

                    OpenTelemetrySpanContextMetadata.mimeType -> {
                        val spanContext = content.read(OpenTelemetrySpanContextMetadata).spanContext
                        Span.wrap(spanContext).asContextElement()
                    }

                    SelectionSetMetadata.mimeType -> {
                        SelectionSetContextElement(content.read(SelectionSetMetadata).selectionSet)
                    }

                    else -> {
                        null
                    }
                }
            }
            // initialize acl element to default.
            // if the element is present in the metadata, the default value will be overridden
            .fold(Dispatchers.IO as CoroutineContext + AclContextElement()) { acc, element ->
                acc + element
            }
    }
}
