package io.eordie.multimodule.organization.repository

import io.eordie.multimodule.example.contracts.organization.models.OrganizationSummary
import io.eordie.multimodule.example.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.example.repository.FilterSupportTrait
import io.eordie.multimodule.example.repository.KFactory
import io.eordie.multimodule.example.repository.KRepository
import io.eordie.multimodule.organization.models.OrganizationModel
import io.micronaut.data.annotation.Query
import java.util.*

@KRepository
interface OrganizationRepository :
    KFactory<OrganizationModel, UUID>,
    FilterSupportTrait<OrganizationModel, UUID, OrganizationsFilter> {

    @Query(
        """
            select count(*) as totalCount, array_agg(id) as organizationIds
            from organization
        """
    )
    suspend fun getOrganizationSummary(filter: OrganizationsFilter): OrganizationSummary
}
