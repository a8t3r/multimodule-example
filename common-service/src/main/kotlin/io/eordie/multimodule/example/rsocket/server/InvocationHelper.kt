package io.eordie.multimodule.example.rsocket.server

import io.eordie.multimodule.example.contracts.annotations.Secured
import io.eordie.multimodule.example.contracts.annotations.Valid
import io.eordie.multimodule.example.contracts.basic.exception.ExceptionDefinition
import io.eordie.multimodule.example.rsocket.client.invocation.AuthorizationCheck
import io.eordie.multimodule.example.rsocket.client.invocation.ValidationCheck
import io.eordie.multimodule.example.rsocket.context.AuthenticationContextElement
import io.eordie.multimodule.example.rsocket.meta.AuthenticationMetadata
import io.eordie.multimodule.example.rsocket.meta.ExceptionalMetadata
import io.eordie.multimodule.example.rsocket.meta.OpenTelemetrySpanContextMetadata
import io.eordie.multimodule.example.rsocket.meta.ProtobufPayloadBuilder
import io.eordie.multimodule.example.utils.extendWith
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
        val returnValue = invoke(entries, descriptor, payload)

        return when {
            returnValue is ExceptionDefinition -> buildExceptionPayload(returnValue)
            else -> proto.encodeToPayload(returnValue, descriptor.implementationFunction.returnType)
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
            returnValue is ExceptionDefinition -> flowOf(buildExceptionPayload(returnValue))
            else -> {
                check(targetType.jvmErasure.isSubclassOf(Flow::class))
                val elementType = requireNotNull(targetType.arguments[0].type)
                (returnValue as Flow<*>)
                    .flowOn(context)
                    .map { proto.encodeToPayload(it, elementType) }
            }
        }
    }

    private fun buildExceptionPayload(definition: ExceptionDefinition) = buildPayload {
        data { writeInt(0) }
        compositeMetadata {
            add(ExceptionalMetadata(definition))
        }
    }

    private suspend fun invoke(
        entries: List<CompositeMetadata.Entry>,
        descriptor: ControllerDescriptor,
        payload: Payload
    ): Any? {
        val context = prepareContext(entries)
        val arguments = prepareInvocationArguments(context, descriptor, payload)
        return performInvocation(descriptor, context, arguments)
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
        } catch (@Suppress("TooGenericExceptionCaught") ex: Exception) {
            logger.error(ex) { "unexpected exception" }
            span.recordException(ex)
            ExceptionDefinition(ex.cause ?: ex)
        } finally {
            span.end()
        }
    }

    private suspend fun beforeInvocation(
        descriptor: ControllerDescriptor,
        context: CoroutineContext,
        arguments: Map<KParameter, Any?>
    ): ExceptionDefinition? {
        val argumentsIndex = arguments.mapKeys { it.key.name }

        val exception = AuthorizationCheck.check(
            descriptor.serviceInterface.findAnnotations<Secured>().firstOrNull(),
            descriptor.securedConstraints,
            context[AuthenticationContextElement]?.details
        ) ?: ValidationCheck.check(
            beanLocator,
            descriptor.serviceFunction.valueParameters
                .filter { it.findAnnotations(Valid::class).isNotEmpty() }
                .mapNotNull { param -> argumentsIndex[param.name]?.let { param to it } }
        )

        return if (exception != null) ExceptionDefinition(exception) else null
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
                    AuthenticationMetadata.mimeType ->
                        AuthenticationContextElement(content.read(AuthenticationMetadata).details)

                    OpenTelemetrySpanContextMetadata.mimeType -> {
                        val spanContext = content.read(OpenTelemetrySpanContextMetadata).spanContext
                        Span.wrap(spanContext).asContextElement()
                    }

                    else -> {
                        null
                    }
                }
            }
            .fold(Dispatchers.IO as CoroutineContext) { acc, element ->
                acc + element
            }
    }
}
