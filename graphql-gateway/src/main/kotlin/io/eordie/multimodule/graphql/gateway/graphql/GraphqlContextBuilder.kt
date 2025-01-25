package io.eordie.multimodule.graphql.gateway.graphql

import graphql.GraphQLContext
import io.eordie.multimodule.common.security.context.AclContextElement
import io.micronaut.http.HttpAttributes
import io.micronaut.http.HttpRequest
import io.micronaut.runtime.http.scope.RequestAware
import io.micronaut.runtime.http.scope.RequestScope
import io.opentelemetry.context.Context
import jakarta.inject.Inject

@RequestScope
open class GraphqlContextBuilder : RequestAware {

    private lateinit var request: HttpRequest<*>

    @Inject
    private lateinit var providers: List<AuthenticationProvider>

    open fun prepareContext(builder: GraphQLContext.Builder): GraphQLContext.Builder {
        return request.attributes.get(HttpAttributes.INVOCATION_CONTEXT, GraphQLContext::class.java)
            .map { builder.of(it) }
            .orElseGet { processContext(builder) }
    }

    private fun processContext(builder: GraphQLContext.Builder): GraphQLContext.Builder {
        val auth = providers.firstNotNullOfOrNull { it.authenticate(request) }

        builder.put(ContextKeys.TELEMETRY, Context.current())
        builder.put(ContextKeys.HEADERS, request.headers)
        builder.put(ContextKeys.ACL, AclContextElement())
        if (auth != null) {
            builder.put(ContextKeys.AUTHENTICATION_DETAILS, auth)
        }
        return builder
    }

    open fun buildContext(): GraphQLContext {
        val graphQLContext = prepareContext(GraphQLContext.newContext()).build()
        request.attributes.put(HttpAttributes.INVOCATION_CONTEXT, graphQLContext)
        return graphQLContext
    }

    override fun setRequest(request: HttpRequest<*>) {
        this.request = request
    }
}
