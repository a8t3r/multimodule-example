package io.eordie.multimodule.organization.models

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToOne
import org.babyfish.jimmer.sql.Table
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "organization_member")
interface OrganizationMemberModel {

    @Id
    val id: UUID
    val createdAt: OffsetDateTime

    @OneToOne(mappedBy = "member")
    val employee: OrganizationEmployeeModel?

    @ManyToOne
    val user: UserModel

    @IdView
    val userId: UUID

    @ManyToOne
    val organization: OrganizationModel

    @IdView
    val organizationId: UUID
}
