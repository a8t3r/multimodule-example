package io.eordie.multimodule.organization.management.validation

import io.eordie.multimodule.common.validation.EntityValidator
import io.eordie.multimodule.common.validation.ensureIsAccessible
import io.eordie.multimodule.common.validation.isSimpleString
import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.organization.management.models.OrganizationDepartmentModel
import jakarta.inject.Singleton
import org.valiktor.validate
import java.util.*

@Singleton
class OrganizationDepartmentValidator(
    private val organizations: EntityLoader<Organization, UUID>
) : EntityValidator<OrganizationDepartmentModel> {
    override suspend fun onCreate(value: OrganizationDepartmentModel) {
        validate(value) {
            validate(OrganizationDepartmentModel::organizationId).ensureIsAccessible(organizations)
        }
    }

    override suspend fun onUpdate(value: OrganizationDepartmentModel) {
        validate(value) {
            validate(OrganizationDepartmentModel::name).isSimpleString()
        }
    }
}
