package io.eordie.multimodule.organization.management.models.acl

import io.eordie.multimodule.contracts.organization.models.acl.ByFarmCriterion
import io.eordie.multimodule.organization.management.models.OrganizationDepartmentModel
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OnDissociate
import org.babyfish.jimmer.sql.Table
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import java.util.*

@Entity
@Table(name = "department_farm_binding")
interface ByFarmCriterionModel : io.eordie.multimodule.common.repository.Convertable<ByFarmCriterion> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generatorType = UUIDIdGenerator::class)
    val id: UUID

    @IdView
    val departmentId: UUID

    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    val department: OrganizationDepartmentModel

    @Key
    val farmId: UUID

    val fieldIds: List<UUID>?
}
