package io.eordie.multimodule.organization.models

import io.eordie.multimodule.example.contracts.organization.models.Organization
import io.eordie.multimodule.example.repository.Convertable
import io.eordie.multimodule.example.repository.entity.PermissionAware
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "organization")
interface OrganizationModel : Convertable<Organization>, PermissionAware {

    @Id
    val id: UUID

    val name: String

    val displayName: String

    @OneToMany(mappedBy = "organization")
    val domains: List<OrganizationDomainModel>

    @OneToMany(mappedBy = "organization")
    val members: List<OrganizationMemberModel>

    @OneToMany(mappedBy = "organization")
    val roles: List<OrganizationRoleModel>

    override fun convert(): Organization {
        return Organization(id, name, displayName)
    }
}
