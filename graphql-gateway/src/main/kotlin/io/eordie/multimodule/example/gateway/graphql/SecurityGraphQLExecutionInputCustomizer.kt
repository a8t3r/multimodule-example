package io.eordie.multimodule.example.gateway.graphql

import graphql.ExecutionInput
import io.micronaut.configuration.graphql.GraphQLExecutionInputCustomizer
import io.micronaut.core.async.publisher.Publishers
import io.micronaut.http.HttpAttributes
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.security.authentication.Authentication
import io.opentelemetry.context.Context
import org.reactivestreams.Publisher

class SecurityGraphQLExecutionInputCustomizer : GraphQLExecutionInputCustomizer {

    override fun customize(
        executionInput: ExecutionInput,
        httpRequest: HttpRequest<*>,
        httpResponse: MutableHttpResponse<String>
    ): Publisher<ExecutionInput> {
        val attributes = httpRequest.attributes
        val auth = attributes.get(HttpAttributes.PRINCIPAL.toString(), Authentication::class.java)
        val telemetry = attributes.get(ContextKeys.TELEMETRY.name, Context::class.java)

        return Publishers.just(
            executionInput.transform { builder ->
                builder.graphQLContext { context ->
                    context.of(
                        buildMap {
                            put(ContextKeys.HEADERS, httpRequest.headers)
                            put(ContextKeys.RESPONSE_HEADERS, httpResponse.headers)
                            auth.ifPresent { put(ContextKeys.AUTHENTICATION, it) }
                            telemetry.ifPresent { put(ContextKeys.TELEMETRY, it) }
                        }
                    )
                }
            }
        )
    }
}
