package io.eordie.multimodule.organization.management.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.CreatedByIF
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
interface OrganizationModel : CreatedByIF, Convertable<Organization>, PermissionAwareIF {

    @Id
    @Column(name = "uid")
    val id: UUID

    @Key
    val name: String

    val displayName: String?

    @Column(name = "created_by_user_id")
    val createdByStr: String

    @OneToMany(mappedBy = "organization")
    val domains: List<OrganizationDomainModel>

    @OneToMany(mappedBy = "organization")
    val members: List<OrganizationMemberModel>

    @OneToMany(mappedBy = "organization")
    val departments: List<OrganizationDepartmentModel>

    @OneToMany(mappedBy = "organization")
    val positions: List<OrganizationPositionModel>

    @OneToMany(mappedBy = "organization")
    val employees: List<OrganizationEmployeeModel>
}
