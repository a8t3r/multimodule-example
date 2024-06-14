package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPosition
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.GenericRepository
import java.util.*

@JdbcRepository(dataSource = "keycloak", dialect = Dialect.POSTGRES)
interface OrganizationPositionsRepository : GenericRepository<OrganizationPosition, UUID> {

    @Query(
        nativeQuery = true,
        value =
        """
            with recursive subordinates(id, depth, parents) as (
                select r.id, 0, array[]::uuid[] 
                from organization_positions r 
                where parent_id is null and not r.deleted
                union all 
                select r.id, s.depth + 1, s.parents || r.parent_id 
                from organization_positions r, subordinates s 
                where r.parent_id = s.id and not r.deleted
            ) select unnest(parents) from subordinates where id = :positionId
        """
    )
    fun getParentIds(positionId: UUID): Set<UUID>
}
