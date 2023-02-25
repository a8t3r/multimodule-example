package io.eordie.multimodule.organization.models

import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationEmployee
import io.eordie.multimodule.example.repository.AuditableAware
import io.eordie.multimodule.example.repository.Convertable
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToOne
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "organization_employees")
interface OrganizationEmployeeModel : AuditableAware, Convertable<OrganizationEmployee> {

    @Id
    val id: UUID

    @ManyToOne
    val department: OrganizationDepartmentModel

    @OneToOne
    val member: OrganizationMemberModel

    @ManyToOne
    val position: OrganizationPositionModel

    @IdView
    val positionId: UUID

    override fun convert(): OrganizationEmployee {
        return OrganizationEmployee(member.userId, department.organizationId, department.id, positionId)
    }
}
