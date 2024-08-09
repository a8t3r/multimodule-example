package io.eordie.multimodule.organization.management.config

import io.eordie.multimodule.organization.management.service.suggestion.ResultEnvelope
import io.eordie.multimodule.organization.management.service.suggestion.SuggestionsClient
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires

@Factory
class ApplicationConfig {

    @Bean
    @Requires(env = [ "test" ])
    @Replaces(SuggestionsClient::class)
    fun suggestionsClient(): SuggestionsClient {
        return object : SuggestionsClient {
            override suspend fun suggest(query: String): ResultEnvelope = ResultEnvelope(emptyList())
            override suspend fun findById(query: String): ResultEnvelope = ResultEnvelope(emptyList())
        }
    }
}
