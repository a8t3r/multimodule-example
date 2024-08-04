package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.repository.KFactory
import io.eordie.multimodule.common.repository.KRepository
import io.eordie.multimodule.contracts.organization.models.OrganizationSummary
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.organization.management.models.OrganizationModel
import io.micronaut.data.annotation.Query
import java.util.*

@KRepository
interface OrganizationRepository : KFactory<OrganizationModel, UUID> {

    @Query(
        """
            select 
                count(distinct tb_1_.uid) as totalCount, 
                array_agg(distinct tb_1_.uid) as organizationIds,
                count(distinct u.uid) as usersCount,
                array_agg(distinct u.uid) filter (where u.uid is not null) as userIds
            from organization tb_1_
            left join organization_member om on om.organization_uid = tb_1_.uid
            left join user_entity u on u.uid = om.user_uid and u.enabled and u.first_name is not null and u.last_name is not null 
        """
    )
    suspend fun getOrganizationSummary(filter: OrganizationsFilter): OrganizationSummary
}
