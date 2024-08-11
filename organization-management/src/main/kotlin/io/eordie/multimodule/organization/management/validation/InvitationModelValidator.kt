package io.eordie.multimodule.organization.management.validation

import io.eordie.multimodule.common.validation.EntityValidator
import io.eordie.multimodule.common.validation.ensureIsAccessible
import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationDepartment
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.organization.management.models.InvitationModel
import jakarta.inject.Singleton
import org.valiktor.functions.isEmail
import org.valiktor.validate
import java.util.*

@Singleton
class InvitationModelValidator(
    private val departments: EntityLoader<OrganizationDepartment, UUID>,
    private val positions: EntityLoader<OrganizationPosition, UUID>
) : EntityValidator<InvitationModel> {
    override suspend fun onUpdate(value: InvitationModel) {
        validate(value) { _ ->
            validate(InvitationModel::email).isEmail()

            validate(InvitationModel::departmentId).ensureIsAccessible(departments, true) {
                it.organizationId == value.organizationId
            }

            validate(InvitationModel::positionId).ensureIsAccessible(positions, true) {
                it.organizationId == value.organizationId
            }
        }
    }
}
