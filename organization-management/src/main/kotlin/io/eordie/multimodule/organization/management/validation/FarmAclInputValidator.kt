package io.eordie.multimodule.organization.management.validation

import io.eordie.multimodule.common.validation.EntityValidator
import io.eordie.multimodule.common.validation.ensureIsPresent
import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.acl.FarmAclInput
import jakarta.inject.Singleton
import org.valiktor.validate
import java.util.*

@Singleton
class FarmAclInputValidator(
    private val organizations: EntityLoader<Organization, UUID>
) : EntityValidator<FarmAclInput> {

    override suspend fun onCreate(value: FarmAclInput) {
        validate(value) {
            validate(FarmAclInput::organisationId).ensureIsPresent(organizations)
        }
    }

    override suspend fun onUpdate(value: FarmAclInput) = Unit
}
