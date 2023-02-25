package io.eordie.multimodule.organization.models

import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.example.repository.Convertable
import io.eordie.multimodule.example.repository.entity.UUIDIdentityIF
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "organization_departments")
interface OrganizationDepartmentModel : UUIDIdentityIF, Convertable<OrganizationDepartment> {
    val name: String

    @ManyToOne
    val organization: OrganizationModel

    @IdView
    val organizationId: UUID

    override fun convert(): OrganizationDepartment {
        return OrganizationDepartment(id, name, organizationId)
    }
}
