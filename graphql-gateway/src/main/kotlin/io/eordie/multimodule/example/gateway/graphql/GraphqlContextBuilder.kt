package io.eordie.multimodule.example.gateway.graphql

import graphql.GraphQLContext
import io.micronaut.http.HttpAttributes
import io.micronaut.http.HttpRequest
import io.micronaut.runtime.http.scope.RequestAware
import io.micronaut.runtime.http.scope.RequestScope
import io.micronaut.security.authentication.Authentication
import io.opentelemetry.context.Context

@RequestScope
open class GraphqlContextBuilder : RequestAware {

    private lateinit var request: HttpRequest<*>

    open fun buildContext(): GraphQLContext {
        val telemetry = Context.current()
        val auth = getAuthentication()

        request.attributes.put(ContextKeys.TELEMETRY.name, telemetry)

        return GraphQLContext.newContext()
            .apply { if (auth != null) of(ContextKeys.AUTHENTICATION, auth) }
            .of(ContextKeys.TELEMETRY, telemetry)
            .build()
    }

    private fun getAuthentication(): Authentication? {
        return request.attributes.get(
            HttpAttributes.PRINCIPAL.toString(),
            Authentication::class.java
        ).orElse(null)
    }

    override fun setRequest(request: HttpRequest<*>) {
        this.request = request
    }
}
