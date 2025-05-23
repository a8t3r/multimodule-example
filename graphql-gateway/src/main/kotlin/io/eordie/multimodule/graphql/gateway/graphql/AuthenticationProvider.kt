package io.eordie.multimodule.graphql.gateway.graphql

import io.eordie.multimodule.common.security.AuthenticationDetailsBuilder
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.utils.JsonModule
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.Authentication
import jakarta.inject.Singleton

interface AuthenticationProvider {
    fun authenticate(request: HttpRequest<*>): AuthenticationDetails?
}

@Singleton
@Requires(env = ["test"])
class DummyAuthenticationProvider : AuthenticationProvider {
    override fun authenticate(request: HttpRequest<*>): AuthenticationDetails? {
        val header = request.headers[HttpHeaders.X_AUTH_TOKEN]
        return header?.let { JsonModule.getInstance().decodeFromString(header) }
    }
}

@Singleton
class PrincipalAuthenticationProvider : AuthenticationProvider {
    override fun authenticate(request: HttpRequest<*>): AuthenticationDetails? {
        return getAuthentication(request)?.let { AuthenticationDetailsBuilder.of(it) }
    }

    private fun getAuthentication(request: HttpRequest<*>): Authentication? =
        request.getUserPrincipal(Authentication::class.java).orElse(null)
}
