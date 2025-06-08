package io.eordie.multimodule.graphql.gateway.graphql.instrumentation

import graphql.ExecutionResult
import graphql.ExecutionResultImpl
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.instrumentation.InstrumentationState
import graphql.execution.instrumentation.SimplePerformantInstrumentation
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import io.eordie.multimodule.graphql.gateway.graphql.DataFetcherExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.asPublisher
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

/**
 * The current micronaut-graphql implementation doesn't work properly with kotlin flow, but it does accept reactive Publisher.
 * This instrumentation is a workaround to bypass the current restrictions.
 */
class FlowAsPublisherInstrumentation(private val exceptionHandler: DataFetcherExceptionHandler) : SimplePerformantInstrumentation() {
    override fun instrumentExecutionResult(
        result: ExecutionResult,
        parameters: InstrumentationExecutionParameters,
        state: InstrumentationState?
    ): CompletableFuture<ExecutionResult> {
        return completedFuture(
            result.transform { builder ->
                if (result.getData<Any>() is Flow<*>) {
                    builder.data(
                        result.getData<Flow<Any>>()
                            .catch {exception ->
                                val handlerParameters = DataFetcherExceptionHandlerParameters.newExceptionParameters()
                                    .exception(exception)
                                    .build()

                                emit(ExecutionResultImpl(
                                    null,
                                    exceptionHandler.handleException(handlerParameters).await().errors)
                                )
                            }
                            .asPublisher()
                    )
                }
            }
        )
    }
}
