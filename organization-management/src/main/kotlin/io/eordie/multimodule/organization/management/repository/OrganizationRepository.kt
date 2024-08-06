package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.repository.KFactory
import io.eordie.multimodule.common.repository.KRepository
import io.eordie.multimodule.contracts.organization.OrganizationDigest
import io.eordie.multimodule.contracts.organization.models.OrganizationFilterSummary
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.organization.management.models.OrganizationModel
import io.micronaut.data.annotation.Query
import kotlinx.coroutines.flow.Flow
import java.util.*

@KRepository
interface OrganizationRepository : KFactory<OrganizationModel, UUID> {

    @Query(
        """
            select 
                tb_1_.uid as organizationId, 
                coalesce(m.count, 0) as membersCount,
                coalesce(d.count, 0) as domainsCount,
                coalesce(s.count, 0) as departmentsCount,
                coalesce(s.count, 0) as positionsCount,
                coalesce(e.count, 0) as employeesCount
            from organization tb_1_
            left join (select organization_uid, count(*) as count from organization_member group by 1) m on tb_1_.uid = m.organization_uid 
            left join (select organization_uid, count(*) as count from organization_domain group by 1) d on tb_1_.uid = d.organization_uid 
            left join (select organization_id, count(*) as count from organization_departments where not deleted group by 1) s on tb_1_.uid = s.organization_id 
            left join (select organization_id, count(*) as count from organization_positions where not deleted group by 1) p on tb_1_.uid = p.organization_id 
            left join (select organization_id, count(*) as count from organization_employees where not deleted group by 1) e on tb_1_.uid = e.organization_id 
        """
    )
    suspend fun getOrganizationsDigest(filter: OrganizationsFilter): Flow<OrganizationDigest>

    @Query(
        """
            select 
                count(distinct tb_1_.uid) as totalCount, 
                array_agg(distinct tb_1_.uid) as organizationIds,
                array_agg(distinct od.uid) filter (where od.uid is not null) as domainIds,
                array_agg(distinct u.uid) filter (where u.uid is not null) as userIds
            from organization tb_1_
            left join organization_member om on om.organization_uid = tb_1_.uid
            left join organization_domain od on od.organization_uid = tb_1_.uid
            left join user_entity u on u.uid = om.user_uid and u.enabled and u.first_name is not null and u.last_name is not null 
        """
    )
    suspend fun getOrganizationSummary(filter: OrganizationsFilter): OrganizationFilterSummary
}
