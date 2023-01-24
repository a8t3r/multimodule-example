package io.eordie.multimodule.example.gateway.security

import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.AuthenticationResponse.success
import io.micronaut.security.oauth2.endpoint.authorization.state.State
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import org.reactivestreams.Publisher

@Singleton
@Named("keycloak")
class KeycloakUserDetailsMapper : OauthAuthenticationMapper {

    @Value("\${micronaut.security.oauth2.clients.keycloak.clientId}")
    lateinit var clientId: String

    @Value("\${micronaut.security.oauth2.clients.keycloak.clientSecret}")
    lateinit var clientSecret: String

    @Value("\${keycloak.host}")
    lateinit var keycloakHost: String

    @Value("\${keycloak.realm}")
    lateinit var keycloakRealm: String

    @Inject
    @Client
    private lateinit var client: HttpClient

    override fun createAuthenticationResponse(
        tokenResponse: TokenResponse,
        state: State?
    ): Publisher<AuthenticationResponse> {
        val res = client.exchange(
            HttpRequest.POST(
                "$keycloakHost/auth/realms/$keycloakRealm/protocol/openid-connect/token/introspect",
                "token=${tokenResponse.accessToken}"
            )
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .basicAuth(clientId, clientSecret), KeycloakUser::class.java
        ).asFlow()

        return res.map { response ->
            val user = requireNotNull(response.body())
            val attrs = mapOf(
                "openIdToken" to tokenResponse.accessToken,
                "username" to user.username,
                "emailVerified" to user.emailVerified,
                "active" to user.active
            )
            success(requireNotNull(user.sub), user.roles ?: emptyList(), attrs)
        }.asPublisher()
    }
}
