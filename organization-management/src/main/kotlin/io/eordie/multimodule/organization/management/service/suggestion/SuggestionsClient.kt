package io.eordie.multimodule.organization.management.service.suggestion

import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Header(name = "Authorization", value = "Token \${integrations.dadata.token}")
@Client("https://suggestions.dadata.ru/suggestions/api/4_1/rs")
interface SuggestionsClient {
    @Post("/suggest/party")
    suspend fun suggest(query: String): ResultEnvelope

    @Post("/findById/party")
    suspend fun findById(query: String): ResultEnvelope
}
