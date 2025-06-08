package io.eordie.multimodule.graphql.gateway.graphql.instrumentation

import graphql.ExecutionResult
import graphql.execution.instrumentation.InstrumentationState
import graphql.execution.instrumentation.SimplePerformantInstrumentation
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asPublisher
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

/**
 * The current micronaut-graphql implementation doesn't work properly with kotlin flow, but it does accept reactive Publisher.
 * This instrumentation is a workaround to bypass the current restrictions.
 */
class FlowAsPublisherInstrumentation : SimplePerformantInstrumentation() {
    override fun instrumentExecutionResult(
        result: ExecutionResult,
        parameters: InstrumentationExecutionParameters,
        state: InstrumentationState?
    ): CompletableFuture<ExecutionResult> {
        return completedFuture(
            result.transform {
                if (result.getData<Any>() is Flow<*>) {
                    it.data(result.getData<Flow<Any>>().asPublisher())
                }
            }
        )
    }
}
