package io.eordie.multimodule.graphql.gateway.security

import com.nimbusds.jwt.JWTParser
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.cookie.Cookie
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.config.RedirectConfiguration
import io.micronaut.security.config.RedirectService
import io.micronaut.security.errors.OauthErrorResponseException
import io.micronaut.security.errors.ObtainingAuthorizationErrorCode
import io.micronaut.security.oauth2.endpoint.token.response.IdTokenLoginHandler
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper.REFRESH_TOKEN_KEY
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdAuthenticationMapper.OPENID_TOKEN_KEY
import io.micronaut.security.token.cookie.AccessTokenCookieConfiguration
import io.micronaut.security.token.cookie.RefreshTokenCookieConfiguration
import jakarta.inject.Singleton
import java.time.Duration
import java.util.*

@Singleton
@Replaces(bean = IdTokenLoginHandler::class)
class RefreshedIdTokenLoginHandler(
    private val refreshTokenCookieConfiguration: RefreshTokenCookieConfiguration,
    accessTokenCookieConfiguration: AccessTokenCookieConfiguration,
    redirectConfiguration: RedirectConfiguration,
    redirectService: RedirectService
) : IdTokenLoginHandler(
    accessTokenCookieConfiguration,
    redirectConfiguration,
    redirectService,
    null
) {

    private val logger = KotlinLogging.logger {}

    private fun errorResponseException(tokenType: String): RuntimeException = OauthErrorResponseException(
        ObtainingAuthorizationErrorCode.SERVER_ERROR,
        "Cannot obtain an $tokenType",
        null
    )

    override fun getCookies(authentication: Authentication, request: HttpRequest<*>): MutableList<Cookie> {
        val cookies: MutableList<Cookie> = ArrayList(2)
        val accessToken = parseIdToken(authentication).orElseThrow { errorResponseException(OPENID_TOKEN_KEY) }

        val jwtCookie = Cookie.of(accessTokenCookieConfiguration.cookieName, accessToken)
        jwtCookie.configure(accessTokenCookieConfiguration, request.isSecure)
        jwtCookie.maxAge(getTokenExpiration(accessToken))
        cookies.add(jwtCookie)

        val refreshToken = Optional.ofNullable(authentication.attributes[REFRESH_TOKEN_KEY] as? String)
            .orElseThrow { errorResponseException(REFRESH_TOKEN_KEY) }

        val refreshCookie = Cookie.of(refreshTokenCookieConfiguration.cookieName, refreshToken)
        refreshCookie.configure(refreshTokenCookieConfiguration, request.isSecure)
        refreshCookie.maxAge(getTokenExpiration(refreshToken))
        cookies.add(refreshCookie)

        return cookies
    }

    private fun getTokenExpiration(token: String): Duration {
        val expirationTime = kotlin.runCatching { JWTParser.parse(token) }
            .getOrNull()?.jwtClaimsSet?.expirationTime

        return if (expirationTime != null) Duration.between(Date().toInstant(), expirationTime.toInstant()) else {
            logger.warn { "unable to define a cookie expiration because id token exp claim is not set" }
            Duration.ofSeconds(0)
        }
    }
}
