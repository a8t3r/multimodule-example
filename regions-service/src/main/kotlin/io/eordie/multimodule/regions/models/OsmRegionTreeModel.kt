package io.eordie.multimodule.regions.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.contracts.regions.models.Region
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.OneToOne
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "regions_tree")
interface OsmRegionTreeModel : Convertable<Region> {
    @Id
    val id: Long

    val parentId: Long?

    val country: String

    val depth: Int

    @IdView
    val relationId: Long

    @OneToOne
    val relation: OsmRelationModel
}
