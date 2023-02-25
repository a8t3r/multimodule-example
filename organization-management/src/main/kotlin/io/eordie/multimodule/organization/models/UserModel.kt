package io.eordie.multimodule.organization.models

import io.eordie.multimodule.example.contracts.organization.models.User
import io.eordie.multimodule.example.repository.Convertable
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.JoinTable
import org.babyfish.jimmer.sql.ManyToMany
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "user_entity")
interface UserModel : Convertable<User> {
    @Id
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

    @ManyToMany
    @JoinTable(
        name = "user_organization_role_mapping",
        joinColumnName = "user_id",
        inverseJoinColumnName = "role_id"
    )
    val roles: List<OrganizationRoleModel>

    fun organizationIds(): List<UUID> = membership.map { it.organizationId }

    private fun String?.default(): String {
        return this ?: "<unspecified>"
    }

    override fun convert(): User {
        return User(id, firstName.default(), lastName.default(), email, emailVerified, enabled)
    }
}
