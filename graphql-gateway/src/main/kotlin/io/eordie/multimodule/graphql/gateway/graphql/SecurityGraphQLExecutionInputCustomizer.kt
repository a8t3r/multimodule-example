package io.eordie.multimodule.graphql.gateway.graphql

import graphql.ExecutionInput
import io.micronaut.configuration.graphql.GraphQLExecutionInputCustomizer
import io.micronaut.core.async.publisher.Publishers
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import org.reactivestreams.Publisher

class SecurityGraphQLExecutionInputCustomizer : GraphQLExecutionInputCustomizer {

    override fun customize(
        executionInput: ExecutionInput,
        httpRequest: HttpRequest<*>,
        httpResponse: MutableHttpResponse<String>? // null response on ws sessions
    ): Publisher<ExecutionInput> {
        return Publishers.just(
            executionInput.transform { builder ->
                builder.graphQLContext { context ->
                    httpResponse?.let { context.put(ContextKeys.RESPONSE_HEADERS, it.headers) }

                    GraphqlContextBuilder().apply {
                        setRequest(httpRequest)
                    }.prepareContext(context)
                }
            }
        )
    }
}
