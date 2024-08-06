package io.eordie.multimodule.organization.management.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.PermissionAwareIF
import io.eordie.multimodule.contracts.organization.models.Organization
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "organization")
interface OrganizationModel : Convertable<Organization>, PermissionAwareIF {

    @Id
    @Column(name = "uid")
    val id: UUID

    @Key
    val name: String

    val displayName: String

    @OneToMany(mappedBy = "organization")
    val domains: List<OrganizationDomainModel>

    @OneToMany(mappedBy = "organization")
    val members: List<OrganizationMemberModel>
}
