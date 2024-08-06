package io.eordie.multimodule.organization.management.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.OrganizationOwnerIF
import io.eordie.multimodule.common.repository.entity.UUIDIdentityIF
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.organization.management.models.acl.ByFarmCriterionModel
import io.eordie.multimodule.organization.management.models.acl.ByRegionCriterionModel
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OnDissociate
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "organization_departments")
interface OrganizationDepartmentModel : UUIDIdentityIF, OrganizationOwnerIF, Convertable<OrganizationDepartment> {

    @Key
    val name: String

    @IdView
    val organizationId: UUID

    @Key
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val organization: OrganizationModel

    @OneToMany(mappedBy = "department")
    val employees: List<OrganizationEmployeeModel>

    val globalBinding: Boolean?

    @OneToMany(mappedBy = "department")
    val regionBindings: List<ByRegionCriterionModel>

    @OneToMany(mappedBy = "department")
    val farmBindings: List<ByFarmCriterionModel>
}
