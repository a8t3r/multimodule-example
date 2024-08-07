package io.eordie.multimodule.contracts.organization.services

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.organization.models.extra.OrganizationPublicInformation
import io.eordie.multimodule.contracts.organization.models.extra.OrganizationPublicInformationInput

@AutoService(Query::class)
interface SuggestionQueries : Query {
    suspend fun suggestions(filter: OrganizationPublicInformationInput): List<OrganizationPublicInformation>
}
