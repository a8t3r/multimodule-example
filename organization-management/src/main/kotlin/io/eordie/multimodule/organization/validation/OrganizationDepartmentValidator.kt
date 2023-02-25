package io.eordie.multimodule.organization.validation

import io.eordie.multimodule.example.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.example.contracts.organization.models.Organization
import io.eordie.multimodule.example.validation.EntityValidator
import io.eordie.multimodule.example.validation.ensureIsPresent
import io.eordie.multimodule.example.validation.isSimpleString
import io.eordie.multimodule.organization.models.OrganizationDepartmentModel
import io.konform.validation.Validation
import jakarta.inject.Singleton
import java.util.*
import kotlin.coroutines.coroutineContext

@Singleton
class OrganizationDepartmentValidator(
    private val organizations: EntityLoader<Organization, UUID>
) : EntityValidator<OrganizationDepartmentModel> {

    override suspend fun onCreate(): Validation<OrganizationDepartmentModel> {
        val context = coroutineContext
        return Validation {
            OrganizationDepartmentModel::organizationId {
                ensureIsPresent(context, organizations)
            }
        }
    }

    override suspend fun onUpdate(): Validation<OrganizationDepartmentModel> = Validation {
        OrganizationDepartmentModel::name {
            isSimpleString()
        }
    }
}
