package io.eordie.multimodule.organization.management.service.suggestion

import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Header(name = "Authorization", value = "Token \${integrations.dadata.token}")
@Client("https://suggestions.dadata.ru")
interface SuggestionsClient {
    @Post("/suggestions/api/4_1/rs/suggest/party")
    suspend fun suggest(query: String): ResultEnvelope

    @Post("/suggestions/api/4_1/rs/findById/party")
    suspend fun findById(query: String): ResultEnvelope
}
