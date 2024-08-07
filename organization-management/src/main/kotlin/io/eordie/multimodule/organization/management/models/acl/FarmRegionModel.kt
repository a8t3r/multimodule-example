package io.eordie.multimodule.organization.management.models.acl

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import java.util.*

@Entity
@Table(name = "farm_regions")
interface FarmRegionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.USER, generatorType = UUIDIdGenerator::class)
    val farmId: UUID

    @OneToMany(mappedBy = "farmRegion")
    val farmAcl: List<FarmAclModel>

    val regionIds: List<Long>
}
