package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.contracts.organization.models.acl.ByFarmCriterion
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.GenericRepository
import java.util.*

@JdbcRepository(dataSource = "keycloak", dialect = Dialect.POSTGRES)
interface ByFarmCriterionRepository : GenericRepository<ByFarmCriterion, UUID> {

    @Query(
        nativeQuery = true,
        value = "delete from department_farm_binding where farm_id = :farmId"
    )
    suspend fun deleteByFarmId(farmId: UUID)

    @Query(
        nativeQuery = true,
        value =
        """
            update department_farm_binding b
            set field_ids = array_intersect(field_ids, :fieldIds)
            from organization_departments d                           
            where d.organization_id = :organizationId and d.id = b.department_id and 
            farm_id = :farmId and field_ids is not null
        """
    )
    suspend fun updateFarmCriteria(organizationId: UUID, farmId: UUID, fieldIds: List<UUID>)
}
