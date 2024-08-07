package io.eordie.multimodule.organization.management.models.acl

import io.eordie.multimodule.organization.management.models.OrganizationDepartmentModel
import io.eordie.multimodule.organization.management.models.OrganizationModel
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "department_bindings")
interface DepartmentBindingView {

    @Id // fake id
    val farmId: UUID

    @IdView
    val organizationId: UUID

    @IdView
    val departmentId: UUID

    @IdView
    val farmOwnerOrganizationId: UUID

    @ManyToOne
    val organization: OrganizationModel

    @ManyToOne
    val department: OrganizationDepartmentModel

    @ManyToOne
    val farmOwnerOrganization: OrganizationModel

    val farmRegionIds: List<Long>

    val roleIds: List<Int>

    val fieldIds: List<UUID>?
}
