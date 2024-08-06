package io.eordie.multimodule.organization.management.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.contracts.organization.models.OrganizationDomain
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OnDissociate
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "organization_domain")
interface OrganizationDomainModel : Convertable<OrganizationDomain> {

    @Id
    @Column(name = "uid")
    val id: UUID

    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)
    @JoinColumn(name = "organization_uid", referencedColumnName = "uid")
    val organization: OrganizationModel

    val domain: String

    val verified: Boolean

    @IdView
    val organizationId: UUID
}
