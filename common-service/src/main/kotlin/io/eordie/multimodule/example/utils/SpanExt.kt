package io.eordie.multimodule.example.utils

import io.eordie.multimodule.example.contracts.utils.Roles
import io.eordie.multimodule.example.rsocket.context.AuthenticationContextElement
import io.eordie.multimodule.example.rsocket.context.getAuthenticationContext
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.extension.kotlin.getOpenTelemetryContext
import kotlin.coroutines.CoroutineContext

fun <T> CoroutineContext.ifMissingRole(required: Roles, block: () -> T?): T? {
    val authentication = this.getAuthenticationContext()
    return if (!authentication.hasRole(required)) block() else null
}

fun <T> CoroutineContext.ifHasAnyRole(vararg required: Roles, block: () -> T?): T? {
    val authentication = this.getAuthenticationContext()
    return if (authentication.hasAnyRole(*required)) block() else {
        val span = Span.fromContext(getOpenTelemetryContext())
        span.addEvent(
            "insufficient.roles",
            Attributes.of(
                AttributeKey.stringArrayKey("missingOneOf"),
                (required.toSet() - getAuthenticationContext().roles.toSet()).map { it.humanName() }.toList(),
            )
        )

        null
    }
}

fun SpanBuilder.extendWith(context: CoroutineContext): SpanBuilder {
    val authentication = context[AuthenticationContextElement]?.details
    if (authentication != null) {
        setAttribute("user_id", authentication.userId.toString())
        setAttribute("user_roles", authentication.roles.toString())
        authentication.currentOrganizationId?.let {
            setAttribute("user_organization_id", it.toString())
        }
    }

    return this
}
