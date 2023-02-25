package io.eordie.multimodule.example.gateway.security

import io.eordie.multimodule.example.contracts.identitymanagement.models.OAuthToken
import io.micronaut.cache.SyncCache
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.security.authentication.ServerAuthentication
import io.micronaut.security.event.LoginSuccessfulEvent
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper.ACCESS_TOKEN_KEY
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper.REFRESH_TOKEN_KEY
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.*

@Singleton
class OAuthResponsePersistence(
    @Named("oauth2") private val cache: SyncCache<*>
) : ApplicationEventListener<LoginSuccessfulEvent> {

    override fun onApplicationEvent(event: LoginSuccessfulEvent) {
        val authentication = event.source as ServerAuthentication
        val attributes = authentication.attributes
        save(
            UUID.fromString(authentication.name),
            OAuthToken(
                attributes[ACCESS_TOKEN_KEY] as String,
                attributes[REFRESH_TOKEN_KEY] as String
            )
        )
    }

    fun saveRefreshToken(userId: UUID, response: TokenResponse) {
        save(userId, OAuthToken(response.accessToken, response.refreshToken))
    }

    private fun save(userId: UUID, token: OAuthToken) {
        cache.put(userId, token)
    }

    fun getRefreshToken(userId: UUID): OAuthToken {
        return cache.get(userId, OAuthToken::class.java).orElseThrow()
    }
}
