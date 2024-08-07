package io.eordie.multimodule.organization.management.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.UUIDIdentityIF
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequest
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequestStatus
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "accession_requests")
interface AccessionRequestModel : UUIDIdentityIF, Convertable<AccessionRequest> {

    val vat: String

    val status: AccessionRequestStatus

    val rejectionMessage: String?

    val organizationId: UUID

    @IdView
    val initiatedById: UUID

    @IdView
    val processedById: UUID?

    @ManyToOne
    val initiatedBy: UserModel

    @ManyToOne
    val processedBy: UserModel?
}
