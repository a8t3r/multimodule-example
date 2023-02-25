package io.eordie.multimodule.example.gateway.security

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.security.config.SecurityConfiguration
import io.micronaut.security.oauth2.client.OpenIdProviderMetadata
import io.micronaut.security.oauth2.configuration.OauthClientConfiguration
import io.micronaut.security.oauth2.endpoint.endsession.request.EndSessionEndpoint
import io.micronaut.security.oauth2.endpoint.endsession.request.EndSessionEndpointResolver
import io.micronaut.security.oauth2.endpoint.endsession.request.OktaEndSessionEndpoint
import io.micronaut.security.oauth2.endpoint.endsession.response.EndSessionCallbackUrlBuilder
import io.micronaut.security.token.reader.TokenResolver
import jakarta.inject.Singleton
import java.util.*
import java.util.function.Supplier

@Singleton
@Replaces(EndSessionEndpointResolver::class)
class EndSessionEndpointResolverReplacement(
    beanContext: BeanContext,
    private val tokenResolver: TokenResolver<HttpRequest<*>>,
    private val securityConfiguration: SecurityConfiguration
) : EndSessionEndpointResolver(beanContext) {

    override fun resolve(
        oauthClientConfiguration: OauthClientConfiguration,
        openIdProviderMetadata: Supplier<OpenIdProviderMetadata?>,
        endSessionCallbackUrlBuilder: EndSessionCallbackUrlBuilder<*>
    ): Optional<EndSessionEndpoint> {
        return Optional.of<EndSessionEndpoint>(
            OktaEndSessionEndpoint(
                endSessionCallbackUrlBuilder,
                oauthClientConfiguration,
                openIdProviderMetadata,
                securityConfiguration,
                tokenResolver
            )
        )
    }
}
