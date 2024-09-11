package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.filter.acceptMany
import io.eordie.multimodule.common.repository.ext.intersection
import io.eordie.multimodule.contracts.organization.models.acl.DepartmentBindingFilter
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartmentFilter
import io.eordie.multimodule.organization.management.models.OrganizationDepartmentModel
import io.eordie.multimodule.organization.management.models.OrganizationDepartmentModelDraft
import io.eordie.multimodule.organization.management.models.acl.ByFarmCriterionModel
import io.eordie.multimodule.organization.management.models.acl.DepartmentBindingView
import io.eordie.multimodule.organization.management.models.acl.department
import io.eordie.multimodule.organization.management.models.acl.departmentId
import io.eordie.multimodule.organization.management.models.acl.farmId
import io.eordie.multimodule.organization.management.models.acl.farmOwnerOrganizationId
import io.eordie.multimodule.organization.management.models.acl.farmRegionIds
import io.eordie.multimodule.organization.management.models.acl.fieldIds
import io.eordie.multimodule.organization.management.models.acl.organizationId
import io.eordie.multimodule.organization.management.models.bindingViews
import io.eordie.multimodule.organization.management.models.by
import io.eordie.multimodule.organization.management.models.id
import io.eordie.multimodule.organization.management.models.name
import io.eordie.multimodule.organization.management.models.organization
import io.eordie.multimodule.organization.management.models.organizationId
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*

@Singleton
class OrganizationDepartmentFactory :
    BaseOrganizationFactory<OrganizationDepartmentModel, OrganizationDepartmentModelDraft, OrganizationDepartment, UUID, OrganizationDepartmentFilter>(
        OrganizationDepartmentModel::class
    ) {

    override val organizationId = OrganizationDepartmentModel::organizationId

    val defaultFetcher = newFetcher(OrganizationDepartmentModel::class).by {
        allScalarFields()
        organizationId()
        regionBindings { allScalarFields() }
        farmBindings { allScalarFields() }
    }

    suspend fun deleteByFarmId(farmId: UUID) {
        wrapped {
            sql.createDelete(ByFarmCriterionModel::class) {
                where(table.farmId eq farmId)
            }.execute(it)
        }
    }

    suspend fun updateFarmCriteria(organizationId: UUID, farmId: UUID, fieldIds: List<UUID>) {
        wrapped {
            sql.createUpdate(ByFarmCriterionModel::class) {
                where(
                    table.farmId eq farmId,
                    table.fieldIds.isNotNull(),
                    table.department.organizationId eq organizationId
                )

                set(table.fieldIds, table.fieldIds.intersection(fieldIds.toTypedArray()))
            }.execute(it)
        }
    }

    fun findBindingsByFilter(filter: DepartmentBindingFilter): List<DepartmentBindingView> {
        return sql.createQuery(DepartmentBindingView::class) {
            where(
                table.departmentId.accept(filter.departmentId),
                table.organizationId.accept(filter.organizationId),
                table.farmOwnerOrganizationId.accept(filter.farmOwnerOrganizationId),
                table.farmId.accept(filter.farmId),
                table.fieldIds.acceptMany(filter.fieldId),
                table.farmRegionIds.acceptMany(filter.regionId)
            )
            select(table)
        }.execute()
    }

    override fun ResourceAcl.toPredicates(
        filter: OrganizationDepartmentFilter,
        table: KNonNullTable<OrganizationDepartmentModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.id.accept(filter.id),
            table.name.accept(filter.name),
            table.organization.accept(filter.organization),
            table.organizationId.accept(filter.organizationId),
            table.bindingViews { farmId.accept(filter.farmId) },
            table.bindingViews { farmRegionIds.acceptMany(filter.regionId) },
            table.bindingViews {
                or(
                    fieldIds.isNull(),
                    fieldIds.acceptMany(filter.fieldId)
                ).takeIf { filter.fieldId != null }
            }
        )
    }
}
