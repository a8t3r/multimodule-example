package io.eordie.multimodule.organization.models

import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.example.repository.AuditableAware
import io.eordie.multimodule.example.repository.Convertable
import io.eordie.multimodule.example.repository.entity.OrganizationOwnerIF
import io.eordie.multimodule.example.repository.entity.UUIDIdentityIF
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinTable
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToMany
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "organization_positions")
interface OrganizationPositionModel :
    UUIDIdentityIF,
    AuditableAware,
    Convertable<OrganizationPosition>,
    OrganizationOwnerIF {

    @Key
    val name: String

    @ManyToMany
    @JoinTable(
        name = "organization_position_role_mapping",
        joinColumnName = "position_id",
        inverseJoinColumnName = "role_id"
    )
    val roles: List<OrganizationRoleModel>

    @Key
    @ManyToOne
    val parent: OrganizationPositionModel?

    @OneToMany(mappedBy = "parent")
    val subordinates: List<OrganizationPositionModel>

    @IdView
    val parentId: UUID?

    override fun convert(): OrganizationPosition {
        return OrganizationPosition(
            id,
            name,
            organizationId,
            roles.map { it.name },
            parentId,
            deleted,
            createdAt,
            updatedAt
        )
    }
}
