package io.eordie.multimodule.example.gateway.security

import com.nimbusds.jwt.JWTParser
import io.eordie.multimodule.example.contracts.identitymanagement.models.OAuthToken
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.endpoints.TokenRefreshRequest.GRANT_TYPE
import io.micronaut.security.endpoints.TokenRefreshRequest.GRANT_TYPE_REFRESH_TOKEN
import io.micronaut.security.oauth2.configuration.OauthClientConfigurationProperties
import io.micronaut.security.oauth2.endpoint.authorization.state.DefaultState
import io.micronaut.security.oauth2.endpoint.token.response.JWTOpenIdClaims
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdAuthenticationMapper
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdTokenResponse
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import java.net.URI
import java.util.*

@Singleton
class KeycloakRefreshAuthenticator(
    private val mapper: OpenIdAuthenticationMapper,
    private val persistence: OAuthResponsePersistence,
    private val httpClient: HttpClient,
    @Named("keycloak") private val properties: OauthClientConfigurationProperties
) {

    fun getTokenOrRefresh(userId: UUID): OAuthToken {
        var previousValue = persistence.getRefreshToken(userId)
        val claims = JWTParser.parse(previousValue.accessToken).jwtClaimsSet
        if (claims.expirationTime <= Date()) {
            runBlocking {
                refresh(previousValue).awaitSingle()
            }
            previousValue = persistence.getRefreshToken(userId)
        }
        return previousValue
    }

    fun refresh(userId: UUID): Publisher<AuthenticationResponse> {
        val token = persistence.getRefreshToken(userId)
        return refresh(token)
    }

    fun refresh(refreshCookie: String): Publisher<AuthenticationResponse> {
        val claims = JWTParser.parse(refreshCookie).jwtClaimsSet
        val token = persistence.getRefreshToken(UUID.fromString(claims.subject))
        if (token.refreshToken != refreshCookie) {
            error("mismatch refresh cookie and persistent value")
        }
        return refresh(token)
    }

    private fun refresh(token: OAuthToken): Publisher<AuthenticationResponse> {
        val httpRequest = buildRefreshRequest(token)

        return Flux.from(httpClient.exchange(httpRequest, OpenIdTokenResponse::class.java))
            .switchMap { response ->
                val tokenResponse = response.body()
                val jwtClaimsSet = JWTParser.parse(tokenResponse.accessToken).jwtClaimsSet
                val claims = JWTOpenIdClaims(jwtClaimsSet)

                persistence.saveRefreshToken(UUID.fromString(jwtClaimsSet.subject), tokenResponse)
                mapper.createAuthenticationResponse(properties.name, tokenResponse, claims, DefaultState())
            }
    }

    private fun buildRefreshRequest(previousValue: OAuthToken): MutableHttpRequest<Map<String, String>> {
        return HttpRequest.POST(
            URI(properties.token.flatMap { it.url }.get()),
            mapOf(
                GRANT_TYPE to GRANT_TYPE_REFRESH_TOKEN,
                GRANT_TYPE_REFRESH_TOKEN to previousValue.refreshToken
            )
        ).apply {
            contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
            basicAuth(properties.clientId, properties.clientSecret)
        }
    }
}
