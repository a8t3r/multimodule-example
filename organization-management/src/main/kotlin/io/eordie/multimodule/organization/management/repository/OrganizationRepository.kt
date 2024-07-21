package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.repository.FilterSupportTrait
import io.eordie.multimodule.common.repository.KFactory
import io.eordie.multimodule.common.repository.KRepository
import io.eordie.multimodule.contracts.organization.models.OrganizationSummary
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.organization.management.models.OrganizationModel
import io.micronaut.data.annotation.Query
import java.util.*

@KRepository
interface OrganizationRepository :
    KFactory<OrganizationModel, UUID>,
    FilterSupportTrait<OrganizationModel, UUID, OrganizationsFilter> {

    @Query(
        """
            select count(*) as totalCount, array_agg(uid) as organizationIds
            from organization tb_1_
        """
    )
    suspend fun getOrganizationSummary(filter: OrganizationsFilter): OrganizationSummary
}
