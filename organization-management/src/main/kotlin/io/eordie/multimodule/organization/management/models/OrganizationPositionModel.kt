package io.eordie.multimodule.organization.management.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.OrganizationOwnerIF
import io.eordie.multimodule.common.repository.entity.UUIDIdentityIF
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.contracts.utils.Roles
import org.babyfish.jimmer.Formula
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OnDissociate
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "organization_positions")
interface OrganizationPositionModel : UUIDIdentityIF, Convertable<OrganizationPosition>, OrganizationOwnerIF {

    @Key
    val name: String

    val roleIds: List<Int>

    @Formula(dependencies = [ "roleIds" ])
    val roles: List<Roles> get() = Roles.fromIds(roleIds)

    @ManyToOne
    val parent: OrganizationPositionModel?

    @OneToMany(mappedBy = "parent")
    val subordinates: List<OrganizationPositionModel>

    @OneToMany(mappedBy = "position")
    val employees: List<OrganizationEmployeeModel>

    @IdView
    val parentId: UUID?

    @IdView
    val organizationId: UUID

    @Key
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val organization: OrganizationModel
}
