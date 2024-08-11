package io.eordie.multimodule.api.tests.config

import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.organization.management.repository.OrganizationFactory
import io.eordie.multimodule.organization.management.service.suggestion.Address
import io.eordie.multimodule.organization.management.service.suggestion.AddressEnvelope
import io.eordie.multimodule.organization.management.service.suggestion.Entry
import io.eordie.multimodule.organization.management.service.suggestion.EntryEnvelope
import io.eordie.multimodule.organization.management.service.suggestion.ResultEnvelope
import io.eordie.multimodule.organization.management.service.suggestion.SuggestionsClient
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.core.convert.ConversionService
import java.util.*

@Factory
class SuggestionClientMock {

    @Bean
    @Requires(env = [ "test" ])
    @Replaces(SuggestionsClient::class)
    fun suggestionsClient(
        conversion: ConversionService,
        organizations: OrganizationFactory
    ): SuggestionsClient {
        return object : SuggestionsClient {
            private suspend fun find(filter: OrganizationsFilter): ResultEnvelope {
                val items = organizations.findByFilter(filter).data
                    .map {
                        val text = it.id.toString()
                        EntryEnvelope(
                            Entry(
                                AddressEnvelope(
                                    Address(0.0, 0.0),
                                    text
                                ),
                                text,
                                text,
                                text
                            ),
                            text
                        )
                    }

                return ResultEnvelope(items)
            }

            override suspend fun suggest(query: String): ResultEnvelope {
                return find(
                    OrganizationsFilter(
                        name = StringLiteralFilter(eq = query)
                    )
                )
            }

            override suspend fun findById(query: String): ResultEnvelope {
                val id = conversion.convert(query, UUID::class.java)
                return if (id.isEmpty) ResultEnvelope(emptyList()) else {
                    find(
                        OrganizationsFilter(
                            id = UUIDLiteralFilter(eq = conversion.convert(query, UUID::class.java).orElse(null))
                        )
                    )
                }
            }
        }
    }
}
