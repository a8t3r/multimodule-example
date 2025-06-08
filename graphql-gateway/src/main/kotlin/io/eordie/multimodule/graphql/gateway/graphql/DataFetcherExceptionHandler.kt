package io.eordie.multimodule.graphql.gateway.graphql

import graphql.ErrorClassification
import graphql.GraphqlErrorBuilder
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import graphql.execution.SimpleDataFetcherExceptionHandler
import io.eordie.multimodule.contracts.basic.exception.BaseRuntimeException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.trace.Span
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.UndeclaredThrowableException
import java.util.concurrent.CompletableFuture
import kotlin.reflect.full.isSubclassOf

class DataFetcherExceptionHandler : SimpleDataFetcherExceptionHandler() {

    private val logger = KotlinLogging.logger {}

    private fun handleExceptionImpl(handlerParameters: DataFetcherExceptionHandlerParameters): DataFetcherExceptionHandlerResult {
        val exception = unwrap(handlerParameters.exception)
        val traceId = Span.current().spanContext.traceId

        val graphQLError = GraphqlErrorBuilder.newError()
            .apply {
                if (handlerParameters.dataFetchingEnvironment != null) {
                    location(handlerParameters.sourceLocation)
                    path(handlerParameters.path)
                }
            }
            .errorType(ErrorClassification.errorClassification(exception::class.simpleName))
            .message(exception.message.orEmpty())
            .apply {
                val extensions = if (exception is BaseRuntimeException) exception.extensions() else emptyMap()
                extensions(extensions + ("traceId" to traceId))
            }
            .build()

        if (!exception::class.isSubclassOf(BaseRuntimeException::class)) {
            logger.error(exception) { "exception during graphql execution" }
        }

        return DataFetcherExceptionHandlerResult.newResult()
            .error(graphQLError)
            .build()
    }

    override fun handleException(handlerParameters: DataFetcherExceptionHandlerParameters): CompletableFuture<DataFetcherExceptionHandlerResult> {
        return CompletableFuture.completedFuture(handleExceptionImpl(handlerParameters))
    }

    override fun unwrap(exception: Throwable): Throwable {
        return when (exception) {
            is UndeclaredThrowableException -> exception.cause?.let { unwrap(it) } ?: super.unwrap(exception)
            is InvocationTargetException -> unwrap(exception.targetException)
            else -> super.unwrap(exception)
        }
    }
}
