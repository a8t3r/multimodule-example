package io.eordie.multimodule.common.security

import io.eordie.multimodule.common.security.context.AuthenticationContextElement
import io.micronaut.http.HttpRequest
import io.micronaut.http.bind.binders.HttpCoroutineContextFactory
import io.micronaut.runtime.http.scope.RequestAware
import io.micronaut.runtime.http.scope.RequestScope
import io.micronaut.security.authentication.Authentication
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@RequestScope
open class AuthenticationCoroutineContextFactory : HttpCoroutineContextFactory<CoroutineContext>, RequestAware {

    private lateinit var request: HttpRequest<*>

    override fun create(): CoroutineContext {
        return request.getUserPrincipal(Authentication::class.java)
            .map { AuthenticationDetailsBuilder.of(it) }
            .map<CoroutineContext> { AuthenticationContextElement(it) }
            .orElse(EmptyCoroutineContext)
    }

    override fun setRequest(request: HttpRequest<*>) {
        this.request = request
    }
}
