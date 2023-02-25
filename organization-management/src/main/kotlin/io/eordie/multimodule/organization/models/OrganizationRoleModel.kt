package io.eordie.multimodule.organization.models

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import java.util.*

@Entity
@Table(name = "organization_role")
interface OrganizationRoleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generatorType = UUIDIdGenerator::class)
    val id: String

    val name: String

    @ManyToOne
    val organization: OrganizationModel

    @IdView
    val organizationId: UUID
}
