package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.contracts.organization.models.acl.EmployeeAcl
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.GenericRepository
import java.util.*

@JdbcRepository(dataSource = "keycloak", dialect = Dialect.POSTGRES)
interface EmployeeAclRepository : GenericRepository<EmployeeAcl, UUID> {

    @Query(
        nativeQuery = true,
        value =
        """
            select bf.id as department_id, bf.farm_owner_organization_id, oe.user_id, bf.farm_id, bf.field_ids,
                   case
                       when (bf.role_ids is null) then '{}'
                       else array_intersect(op.role_ids, bf.role_ids)
                   end as role_ids
            from organization_employees oe
            join organization_positions op on oe.position_id = op.id
            join (select d.id, fa.farm_owner_organization_id,
                         case
                             when (d.global_binding is false) then null
                             else fa.role_ids
                             end as role_ids,
                         case
                             when (d.global_binding is true) then fa.farm_id
                             when (d.global_binding is false) then null
                             else fb.farm_id
                         end as farm_id,
                         case
                             when (d.global_binding is true) then fa.field_ids
                             when (d.global_binding is false) then null
                             when (fb.field_ids is null) then fa.field_ids
                             when (fa.field_ids is null) then fa.field_ids
                             else array_intersect(fa.field_ids, fb.field_ids)
                         end as field_ids
                  from farm_acl fa
                           join organization_departments d on d.organization_id = fa.organization_id
                           left join department_farm_binding fb on fb.department_id = d.id and fa.farm_id = fb.farm_id
                  where not fa.deleted and not d.deleted
            ) bf on bf.id = oe.department_id
            where not oe.deleted and not op.deleted and bf.farm_id is not null and
                oe.organization_id = :organizationId and
                oe.user_id = :userId
        """
    )
    fun findAccessible(organizationId: UUID, userId: UUID): List<EmployeeAcl>
}
