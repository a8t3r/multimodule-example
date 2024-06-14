package io.eordie.multimodule.common.utils

import io.eordie.multimodule.common.rsocket.context.AuthenticationContextElement
import io.opentelemetry.api.trace.SpanBuilder
import kotlin.coroutines.CoroutineContext

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
