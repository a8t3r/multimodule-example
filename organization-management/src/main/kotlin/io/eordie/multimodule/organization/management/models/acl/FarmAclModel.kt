package io.eordie.multimodule.organization.management.models.acl

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.OrganizationOwnerIF
import io.eordie.multimodule.common.repository.entity.PermissionAwareIF
import io.eordie.multimodule.common.repository.entity.UUIDIdentityIF
import io.eordie.multimodule.contracts.organization.models.acl.FarmAcl
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.OrganizationModel
import org.babyfish.jimmer.Formula
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OnDissociate
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "farm_acl")
interface FarmAclModel : UUIDIdentityIF, OrganizationOwnerIF, PermissionAwareIF, Convertable<FarmAcl> {

    @IdView
    val organizationId: UUID

    @Key
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val organization: OrganizationModel

    @IdView("farmRegion")
    val farmId: UUID

    @IdView
    val farmOwnerOrganizationId: UUID

    @Key
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val farmOwnerOrganization: OrganizationModel

    val roleIds: List<Int>

    @Formula(dependencies = [ "roleIds" ])
    val roles: List<Roles> get() = Roles.fromIds(roleIds)

    val fieldIds: List<UUID>?

    @Key
    @ManyToOne
    @JoinColumn(name = "farm_id")
    val farmRegion: FarmRegionModel
}
