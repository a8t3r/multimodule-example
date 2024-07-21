package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.ResourceAcl
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartmentFilter
import io.eordie.multimodule.organization.management.models.OrganizationDepartmentModel
import io.eordie.multimodule.organization.management.models.by
import io.eordie.multimodule.organization.management.models.id
import io.eordie.multimodule.organization.management.models.name
import io.eordie.multimodule.organization.management.models.organizationId
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*

@Singleton
class OrganizationDepartmentFactory :
    BaseOrganizationFactory<OrganizationDepartmentModel, OrganizationDepartment, UUID, OrganizationDepartmentFilter>(
        OrganizationDepartmentModel::class
    ) {

    override val organizationId = OrganizationDepartmentModel::organizationId

    val defaultFetcher = newFetcher(OrganizationDepartmentModel::class).by {
        allScalarFields()
        organizationId()
        regionBindings { allScalarFields() }
        farmBindings { allScalarFields() }
    }

    override fun ResourceAcl.toPredicates(
        filter: OrganizationDepartmentFilter,
        table: KNonNullTable<OrganizationDepartmentModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.id.accept(filter.id),
            table.name.accept(filter.name),
            table.organizationId.accept(filter.organizationId)
        )
    }
}
