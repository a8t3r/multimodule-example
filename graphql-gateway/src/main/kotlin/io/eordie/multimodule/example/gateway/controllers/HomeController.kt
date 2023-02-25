package io.eordie.multimodule.example.gateway.controllers

import io.eordie.multimodule.example.gateway.security.KeycloakRefreshAuthenticator
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.CookieValue
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.handlers.RedirectingLoginHandler
import io.micronaut.security.rules.SecurityRule
import io.micronaut.views.View
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import java.util.*

@Controller
class HomeController(
    private val authenticator: KeycloakRefreshAuthenticator,
    private val loginHandler: RedirectingLoginHandler<HttpRequest<*>, MutableHttpResponse<*>>
) {

    @Get
    @View("home")
    @Secured(SecurityRule.IS_ANONYMOUS)
    fun index(): Map<String, Any> {
        return emptyMap()
    }

    fun refreshByUserId(userId: UUID, request: HttpRequest<*>): Publisher<MutableHttpResponse<*>> =
        authenticator.refresh(userId).processResponse(request)

    @Get("/oauth/access_token")
    @Secured(SecurityRule.IS_ANONYMOUS)
    fun refresh(
        @CookieValue("JWT_REFRESH_TOKEN") cookieRefreshToken: String,
        request: HttpRequest<*>
    ): Publisher<MutableHttpResponse<*>> = authenticator.refresh(cookieRefreshToken).processResponse(request)

    private fun Publisher<AuthenticationResponse>.processResponse(
        request: HttpRequest<*>
    ): Flux<MutableHttpResponse<*>> {
        return Flux.from(this)
            .map { response ->
                if (response.isAuthenticated && response.authentication.isPresent) {
                    val provided = response.authentication.get()
                    loginHandler.loginSuccess(provided, request)
                } else {
                    loginHandler.loginFailed(response, request)
                }
            }
    }
}
