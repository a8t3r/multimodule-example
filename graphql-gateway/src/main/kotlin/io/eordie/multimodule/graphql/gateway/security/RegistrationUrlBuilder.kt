package io.eordie.multimodule.graphql.gateway.security

import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.simple.SimpleHttpRequest
import io.micronaut.security.oauth2.client.OpenIdProviderMetadata
import io.micronaut.security.oauth2.configuration.OauthClientConfiguration
import io.micronaut.security.oauth2.endpoint.authorization.request.AuthorizationRedirectHandler
import io.micronaut.security.oauth2.endpoint.authorization.request.OpenIdAuthorizationRequest
import jakarta.inject.Singleton

@Singleton
class RegistrationUrlBuilder(
    private val context: ApplicationContext,
    private val metadata: OpenIdProviderMetadata,
    private val configuration: OauthClientConfiguration,
    private val redirectUrlBuilder: AuthorizationRedirectHandler
) {

    fun builderUrl(): String {
        val mockRequest = SimpleHttpRequest(HttpMethod.GET, "", null)
        val request = context.createBean(
            OpenIdAuthorizationRequest::class.java,
            mockRequest,
            configuration,
            metadata
        )

        val response = redirectUrlBuilder.redirect(request, metadata.registrationEndpoint)
        return response.headers.get(HttpHeaders.LOCATION)
    }
}
