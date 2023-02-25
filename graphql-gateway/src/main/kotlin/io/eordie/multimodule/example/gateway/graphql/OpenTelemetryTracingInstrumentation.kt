package io.eordie.multimodule.example.gateway.graphql

import graphql.ExecutionResult
import graphql.execution.instrumentation.InstrumentationState
import graphql.execution.instrumentation.SimplePerformantInstrumentation
import graphql.execution.instrumentation.parameters.InstrumentationCreateStateParameters
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class OpenTelemetryTracingInstrumentation(
    private val tracer: Tracer
) : SimplePerformantInstrumentation() {

    data class SpanInstrumentationState(val span: Span) : InstrumentationState

    override fun createStateAsync(
        parameters: InstrumentationCreateStateParameters
    ): CompletableFuture<InstrumentationState> {
        val executionInput = parameters.executionInput
        val telemetryContext = executionInput.graphQLContext.openTelemetryContext()

        val span = tracer.spanBuilder("graphql:${executionInput.operationName}")
            .setAttribute("graphql.query", executionInput.query)
            .apply {
                executionInput.variables.entries.forEach { (key, value) ->
                    setAttribute("graphql.variable.$key", value.toString())
                }
            }
            .setParent(telemetryContext)
            .startSpan()
            .apply { telemetryContext.with(this) }

        return completedFuture(SpanInstrumentationState(span))
    }

    override fun instrumentExecutionResult(
        result: ExecutionResult,
        parameters: InstrumentationExecutionParameters,
        state: InstrumentationState
    ): CompletableFuture<ExecutionResult> {
        val span = (state as SpanInstrumentationState).span
        span.end()
        return completedFuture(
            result.transform {
                it.addExtension("openTelemetry.traceId", span.spanContext.traceId)
            }
        )
    }
}
