package io.eordie.multimodule.graphql.gateway.controllers

import io.eordie.multimodule.common.rsocket.client.route.LocalRouteOnly
import io.eordie.multimodule.common.rsocket.context.getAuthenticationContext
import io.eordie.multimodule.contracts.identitymanagement.models.OAuthToken
import io.eordie.multimodule.contracts.identitymanagement.services.TokenEnhancer
import io.micronaut.core.type.MutableHeaders
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.simple.SimpleHttpRequest
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitSingle
import java.util.*
import kotlin.coroutines.coroutineContext

@Singleton
@LocalRouteOnly
class TokenEnhancerImpl(
    private val homeController: HomeController,
    private val activeOrganization: ActiveOrganizationClient
) : TokenEnhancer {

    override suspend fun switchOrganization(headers: MutableHeaders, token: OAuthToken, organizationId: UUID): Boolean {
        val details = coroutineContext.getAuthenticationContext()
        return if (details.currentOrganizationId == organizationId) false else {
            val updated = activeOrganization.switchOrganization("Bearer ${token.accessToken}", organizationId)
            val success = updated.accessToken != null

            if (success) {
                val mockRequest = SimpleHttpRequest(HttpMethod.GET, "", null)
                val response = homeController.refreshByUserId(details.userId, mockRequest).awaitSingle()
                response.headers.getAll(HttpHeaders.SET_COOKIE).forEach {
                    headers.add(HttpHeaders.SET_COOKIE, it)
                }
            }
            success
        }
    }
}
