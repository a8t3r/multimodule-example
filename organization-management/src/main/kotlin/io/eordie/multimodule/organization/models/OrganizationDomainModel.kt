package io.eordie.multimodule.organization.models

import io.eordie.multimodule.example.contracts.organization.models.OrganizationDomain
import io.eordie.multimodule.example.repository.Convertable
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "organization_domain")
interface OrganizationDomainModel : Convertable<OrganizationDomain> {

    @Id
    val id: UUID

    @ManyToOne
    val organization: OrganizationModel

    val domain: String

    val verified: Boolean

    @IdView
    val organizationId: UUID

    override fun convert(): OrganizationDomain {
        return OrganizationDomain(id, domain, verified, organizationId)
    }
}
