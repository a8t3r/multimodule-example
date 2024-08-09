package io.eordie.multimodule.organization.management.models

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.entity.UUIDIdentityIF
import io.eordie.multimodule.common.utils.JtsUtils
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.organization.models.extra.OrganizationPublicInformation
import org.babyfish.jimmer.Formula
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.OnDissociate
import org.babyfish.jimmer.sql.OneToOne
import org.babyfish.jimmer.sql.Table
import java.util.*

@Entity
@Table(name = "organization_public_information")
interface OrganizationPublicInformationModel : UUIDIdentityIF, Convertable<OrganizationPublicInformation> {

    @IdView
    val organizationId: UUID

    @Key
    @OneToOne
    @OnDissociate(DissociateAction.DELETE)
    val organization: OrganizationModel

    val name: String
    val kpp: String
    val ogrn: String
    val inn: String
    val address: String

    val locationLon: Double?
    val locationLat: Double?

    @Formula(dependencies = [ "locationLon", "locationLat" ])
    val location: TPoint? get() = run {
        val (x, y) = locationLon to locationLat
        return if (x == null || y == null) null else {
            TPoint(JtsUtils.WGS_84, x, y)
        }
    }
}
