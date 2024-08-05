package io.eordie.multimodule.graphql.gateway.graphql

import graphql.GraphQLContext
import io.eordie.multimodule.common.security.context.AclContextElement
import io.eordie.multimodule.common.security.context.AuthenticationContextElement
import io.eordie.multimodule.contracts.basic.exception.UnauthenticatedException
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
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
    return this.get<AuthenticationDetails>(ContextKeys.AUTHENTICATION_DETAILS)
}

fun GraphQLContext.aclElementOrEmpty(): CoroutineContext {
    val context = this.get<AclContextElement>(ContextKeys.ACL)
    return context ?: EmptyCoroutineContext
}

fun GraphQLContext.openTelemetryElement(): CoroutineContext {
    return this.openTelemetryContext().asContextElement()
}

fun GraphQLContext.newCoroutineContext(): CoroutineContext {
    return openTelemetryElement() + authenticationElementOrEmpty() + aclElementOrEmpty()
}

fun GraphQLContext.openTelemetryContext(): Context {
    val context = this.get<Context>(ContextKeys.TELEMETRY)
    return context ?: Context.current().apply { openTelemetryContext(this) }
}

fun GraphQLContext.openTelemetryContext(context: Context) {
    this.put(ContextKeys.TELEMETRY, context)
}
