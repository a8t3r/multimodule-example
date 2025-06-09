package io.eordie.multimodule.graphql.gateway.graphql

import graphql.GraphQLContext
import io.eordie.multimodule.common.security.AuthenticationDetailsBuilder
import io.eordie.multimodule.common.security.context.AclContextElement
import io.micronaut.http.HttpRequest
import io.micronaut.runtime.http.scope.RequestAware
import io.micronaut.runtime.http.scope.RequestScope
import io.micronaut.security.authentication.Authentication
import io.opentelemetry.context.Context

@RequestScope
open class GraphqlContextBuilder : RequestAware {

    companion object {
        private const val INVOCATION_CONTEXT = "INVOCATION_CONTEXT"
    }

    private lateinit var request: HttpRequest<*>

    open fun prepareContext(builder: GraphQLContext.Builder): GraphQLContext.Builder {
        return request.attributes.get(INVOCATION_CONTEXT, GraphQLContext::class.java)
            .map { builder.of(it) }
            .orElseGet { processContext(builder) }
    }

    private fun processContext(builder: GraphQLContext.Builder): GraphQLContext.Builder {
        val auth = request.getUserPrincipal(Authentication::class.java)
            .map { AuthenticationDetailsBuilder.of(it) }

        builder.put(ContextKeys.TELEMETRY, Context.current())
        builder.put(ContextKeys.HEADERS, request.headers)
        builder.put(ContextKeys.ACL, AclContextElement())
        if (auth.isPresent) {
            builder.put(ContextKeys.AUTHENTICATION_DETAILS, auth.get())
        }
        return builder
    }

    open fun buildContext(): GraphQLContext {
        val graphQLContext = prepareContext(GraphQLContext.newContext()).build()
        request.attributes.put(INVOCATION_CONTEXT, graphQLContext)
        return graphQLContext
    }

    override fun setRequest(request: HttpRequest<*>) {
        this.request = request
    }
}
