package io.eordie.multimodule.organization.management.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.contracts.organization.models.User
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "user_entity")
interface UserModel : Convertable<User> {
    @Id
    @Column(name = "uid")
    val id: UUID

    val email: String

    val firstName: String?

    val lastName: String?

    val emailVerified: Boolean

    val enabled: Boolean

    @OneToMany(mappedBy = "user")
    val membership: List<OrganizationMemberModel>

    @OneToMany(mappedBy = "user")
    val attributes: List<UserAttributeModel>

    fun organizationIds(): List<UUID> = membership.map { it.organizationId }
}
