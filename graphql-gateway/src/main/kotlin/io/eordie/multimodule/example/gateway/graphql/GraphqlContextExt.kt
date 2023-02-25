package io.eordie.multimodule.example.gateway.graphql

import graphql.GraphQLContext
import io.eordie.multimodule.example.contracts.basic.exception.UnauthenticatedException
import io.eordie.multimodule.example.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.example.rsocket.context.AuthenticationContextElement
import io.micronaut.security.authentication.Authentication
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun GraphQLContext.authenticationDetails(): AuthenticationDetails {
    return this.authenticationDetailsOrNull() ?: throw UnauthenticatedException()
}

fun GraphQLContext.authenticationElementOrEmpty(): CoroutineContext {
    return this.authenticationDetailsOrNull()?.let { AuthenticationContextElement(it) }
        ?: EmptyCoroutineContext
}

fun GraphQLContext.authenticationDetailsOrNull(): AuthenticationDetails? {
    return this.get<Authentication>(ContextKeys.AUTHENTICATION)
        ?.let { AuthenticationDetailsBuilder.of(it) }
}

fun GraphQLContext.openTelemetryElement(): CoroutineContext {
    return this.openTelemetryContext().asContextElement()
}

fun GraphQLContext.openTelemetryContext(): Context {
    val context = this.get<Context>(ContextKeys.TELEMETRY)
    return context ?: Context.current().apply { openTelemetryContext(this) }
}

fun GraphQLContext.openTelemetryContext(context: Context) {
    this.put(ContextKeys.TELEMETRY, context)
}
