package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.contracts.organization.models.DomainFilter
import io.eordie.multimodule.contracts.organization.models.OrganizationDomain
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.organization.management.models.OrganizationDomainModel
import io.eordie.multimodule.organization.management.models.domain
import io.eordie.multimodule.organization.management.models.id
import io.eordie.multimodule.organization.management.models.verified
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class OrganizationDomainFactory :
    BaseOrganizationFactory<OrganizationDomainModel, OrganizationDomain, UUID, DomainFilter>(
        OrganizationDomainModel::class
    ) {
    override val organizationId = OrganizationDomainModel::organizationId

    override fun ResourceAcl.toPredicates(
        filter: DomainFilter,
        table: KNonNullTable<OrganizationDomainModel>
    ): List<KNonNullExpression<Boolean>> = listOfNotNull(
        table.id.accept(filter.id),
        table.domain.accept(filter.domain),
        table.verified.accept(filter.verified)
    )
}
