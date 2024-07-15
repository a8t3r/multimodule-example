package io.eordie.multimodule.organization.management.models.acl

import io.eordie.multimodule.common.repository.AuditableAware
import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.OrganizationOwnerIF
import io.eordie.multimodule.common.repository.entity.PermissionAwareIF
import io.eordie.multimodule.common.repository.entity.UUIDIdentityIF
import io.eordie.multimodule.contracts.organization.models.acl.FarmAcl
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.OrganizationModel
import org.babyfish.jimmer.Formula
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "farm_acl")
interface FarmAclModel : UUIDIdentityIF, AuditableAware, OrganizationOwnerIF, PermissionAwareIF, Convertable<FarmAcl> {

    @IdView
    val organizationId: UUID

    @Key
    @ManyToOne
    val organization: OrganizationModel

    @Key
    val farmId: UUID

    @IdView
    val farmOwnerOrganizationId: UUID

    @Key
    @ManyToOne
    val farmOwnerOrganization: OrganizationModel

    val roleIds: List<Int>

    @Formula(dependencies = [ "roleIds" ])
    val roles: List<String> get() = Roles.nameFromIds(roleIds)

    val fieldIds: List<UUID>?
}