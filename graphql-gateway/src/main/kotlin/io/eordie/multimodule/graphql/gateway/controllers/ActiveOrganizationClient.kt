package io.eordie.multimodule.graphql.gateway.controllers

import io.eordie.multimodule.contracts.organization.models.Organization
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Put
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.oauth2.endpoint.token.response.OpenIdTokenResponse
import java.util.*

@Client("\${keycloak.baseUri}")
interface ActiveOrganizationClient {
    @Get("/users/active-organization")
    suspend fun activeOrganization(@Header("Authorization") token: String): Organization

    @Put("/users/switch-organization", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    suspend fun switchOrganization(@Header("Authorization") token: String, id: UUID): OpenIdTokenResponse
}
