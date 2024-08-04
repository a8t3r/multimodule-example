package io.eordie.multimodule.organization.management.validation

import io.eordie.multimodule.common.validation.EntityValidator
import io.eordie.multimodule.common.validation.ensureIsAccessible
import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.organization.management.models.OrganizationEmployeeModel
import jakarta.inject.Singleton
import org.valiktor.functions.isNotNull
import org.valiktor.validate
import java.util.*

@Singleton
class OrganizationEmployeeValidator(
    private val organizations: EntityLoader<Organization, UUID>,
    private val departments: EntityLoader<OrganizationDepartment, UUID>,
    private val positions: EntityLoader<OrganizationPosition, UUID>
) : EntityValidator<OrganizationEmployeeModel> {
    override suspend fun onUpdate(value: OrganizationEmployeeModel) {
        validate(value) {
            validate(OrganizationEmployeeModel::organizationId).ensureIsAccessible(organizations)
            validate(OrganizationEmployeeModel::departmentId).ensureIsAccessible(departments, true) {
                it.organizationId == value.organizationId
            }
            validate(OrganizationEmployeeModel::positionId).isNotNull().ensureIsAccessible(positions) {
                it.organizationId == value.organizationId
            }
        }
    }
}
