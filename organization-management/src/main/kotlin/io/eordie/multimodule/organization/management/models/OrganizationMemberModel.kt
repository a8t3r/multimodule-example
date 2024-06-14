package io.eordie.multimodule.organization.management.models

import io.eordie.multimodule.common.repository.entity.CreatedAtIF
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import java.util.*

@Entity
@Table(name = "organization_member")
interface OrganizationMemberModel : CreatedAtIF {

    @Id
    @Column(name = "uid")
    @GeneratedValue(strategy = GenerationType.AUTO, generatorType = UUIDIdGenerator::class)
    val id: UUID

    @Key
    @ManyToOne
    @JoinColumn(name = "user_uid", referencedColumnName = "uid")
    val user: UserModel

    @IdView
    val userId: UUID

    @Key
    @ManyToOne
    @JoinColumn(name = "organization_uid", referencedColumnName = "uid")
    val organization: OrganizationModel

    @IdView
    val organizationId: UUID
}
