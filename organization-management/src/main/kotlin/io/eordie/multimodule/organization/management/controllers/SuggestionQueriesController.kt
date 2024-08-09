package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.contracts.basic.loader.loadOne
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.extra.OrganizationPublicInformation
import io.eordie.multimodule.contracts.organization.models.extra.OrganizationPublicInformationFilter
import io.eordie.multimodule.contracts.organization.services.SuggestionQueries
import io.eordie.multimodule.organization.management.service.suggestion.ResultEnvelope
import io.eordie.multimodule.organization.management.service.suggestion.SuggestionsClient
import io.micronaut.http.annotation.Controller
import java.util.*
import kotlin.coroutines.coroutineContext

@Controller
class SuggestionQueriesController(
    private val client: SuggestionsClient,
    private val organizationLoader: EntityLoader<Organization, UUID>,
    private val organizationInformationLoader: EntityLoader<OrganizationPublicInformation, UUID>
) : SuggestionQueries {

    override suspend fun suggestions(filter: OrganizationPublicInformationFilter): List<OrganizationPublicInformation> {
        val filterBy = if (filter.organizationId == null) filter else {
            val information = organizationInformationLoader.loadOne(coroutineContext, filter.organizationId)
            if (information?.inn != null) {
                filter.copy(vat = information.inn)
            } else {
                val organization = organizationLoader.loadOne(coroutineContext, filter.organizationId)
                filter.copy(query = organization?.name)
            }
        }

        val result = filterBy.query?.let { client.suggest(it) }
            ?: run { filterBy.vat?.let { client.findById(it) } }
            ?: run { ResultEnvelope(emptyList()) }

        return result.convert()
    }
}
