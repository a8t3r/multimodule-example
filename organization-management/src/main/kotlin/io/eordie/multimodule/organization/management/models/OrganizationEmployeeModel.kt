package io.eordie.multimodule.organization.management.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.CreatedByIF
import io.eordie.multimodule.common.repository.entity.OrganizationOwnerIF
import io.eordie.multimodule.common.repository.entity.UUIDIdentityIF
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationEmployee
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
@Table(name = "organization_employees")
interface OrganizationEmployeeModel :
    UUIDIdentityIF,
    OrganizationOwnerIF,
    CreatedByIF,
    Convertable<OrganizationEmployee> {

    @IdView
    val memberId: UUID?

    @Key
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    @JoinColumn(referencedColumnName = "uid")
    val member: OrganizationMemberModel?

    @IdView
    val userId: UUID

    @ManyToOne
    @JoinColumn(referencedColumnName = "uid")
    val user: UserModel

    @IdView
    val departmentId: UUID?

    @Key
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val department: OrganizationDepartmentModel?

    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val position: OrganizationPositionModel?

    @IdView
    val positionId: UUID?

    @IdView
    val organizationId: UUID

    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    @JoinColumn(referencedColumnName = "uid")
    val organization: OrganizationModel
}
