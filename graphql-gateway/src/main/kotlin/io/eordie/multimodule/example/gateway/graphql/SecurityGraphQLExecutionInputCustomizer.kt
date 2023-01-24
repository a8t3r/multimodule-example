package io.eordie.multimodule.example.gateway.graphql

import graphql.ExecutionInput
import io.micronaut.configuration.graphql.GraphQLExecutionInputCustomizer
import io.micronaut.core.async.publisher.Publishers
import io.micronaut.http.HttpAttributes
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.security.authentication.Authentication
import org.reactivestreams.Publisher

class SecurityGraphQLExecutionInputCustomizer : GraphQLExecutionInputCustomizer {

    override fun customize(
        executionInput: ExecutionInput,
        httpRequest: HttpRequest<*>,
        httpResponse: MutableHttpResponse<String>?
    ): Publisher<ExecutionInput> {
        val auth = httpRequest.attributes.get(
            HttpAttributes.PRINCIPAL.toString(), Authentication::class.java
        )

        return Publishers.just(
            executionInput.transform { builder ->
                builder.graphQLContext { context ->
                    auth.ifPresent { context.of(HttpAttributes.PRINCIPAL, it) }
                }
            }
        )
    }
}
