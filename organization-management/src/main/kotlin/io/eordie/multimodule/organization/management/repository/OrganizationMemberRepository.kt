package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.contracts.organization.models.User
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.GenericRepository
import java.util.*

@JdbcRepository(dataSource = "keycloak", dialect = Dialect.POSTGRES)
interface OrganizationMemberRepository : GenericRepository<User, UUID> {
    @Query(
        nativeQuery = true,
        value = """
            insert into organization_member (id, created_at, user_id, organization_id) 
            values (uuid_generate_v4(), current_timestamp, cast(:userId as text), cast(:organizationId as text))
            returning uid
        """
    )
    fun addMemberToOrganization(userId: UUID, organizationId: UUID): UUID
}
