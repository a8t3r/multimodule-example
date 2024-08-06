package io.eordie.multimodule.organization.management.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.OrganizationOwnerIF
import io.eordie.multimodule.common.repository.entity.UUIDIdentityIF
import io.eordie.multimodule.contracts.organization.models.invitation.Invitation
import io.eordie.multimodule.contracts.organization.models.invitation.InvitationStatus
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OnDissociate
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "organization_invitations")
interface InvitationModel : UUIDIdentityIF, OrganizationOwnerIF, Convertable<Invitation> {

    @Key
    val email: String

    @IdView
    val userId: UUID?

    @ManyToOne
    val user: UserModel?

    @IdView
    val organizationId: UUID

    @Key
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val organization: OrganizationModel

    val status: InvitationStatus

    val departmentId: UUID?

    val positionId: UUID?
}
