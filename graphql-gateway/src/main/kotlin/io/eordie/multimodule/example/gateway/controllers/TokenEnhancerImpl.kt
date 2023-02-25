package io.eordie.multimodule.example.gateway.controllers

import io.eordie.multimodule.example.contracts.identitymanagement.services.TokenEnhancer
import io.eordie.multimodule.example.contracts.organization.services.UserMutations
import io.eordie.multimodule.example.rsocket.context.getAuthenticationContext
import io.micronaut.core.type.MutableHeaders
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.simple.SimpleHttpRequest
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitSingle
import java.util.*
import kotlin.coroutines.coroutineContext

@Singleton
class TokenEnhancerImpl(
    private val userMutations: UserMutations,
    private val homeController: HomeController
) : TokenEnhancer {

    override suspend fun switchOrganization(headers: MutableHeaders, organizationId: UUID): Boolean {
        val details = coroutineContext.getAuthenticationContext()
        return if (details.currentOrganizationId == organizationId) false else {
            val success = userMutations.switchOrganization(details.userId, organizationId)
            if (success) {
                val mockRequest = SimpleHttpRequest(HttpMethod.GET, "", null)
                val response = homeController.refreshByUserId(details.userId, mockRequest).awaitSingle()
                headers[HttpHeaders.SET_COOKIE] = response.headers[HttpHeaders.SET_COOKIE]
            }
            success
        }
    }
}
