package io.eordie.multimodule.organization.management.validation

import io.eordie.multimodule.common.rsocket.context.getAuthentication
import io.eordie.multimodule.common.validation.EntityValidator
import io.eordie.multimodule.common.validation.IsPresent
import io.eordie.multimodule.contracts.organization.models.acl.ByFarmCriterion
import io.eordie.multimodule.organization.management.repository.FarmAclFactory
import jakarta.inject.Singleton
import org.valiktor.validate

@Singleton
class ByFarmCriterionValidator(
    val acl: FarmAclFactory
) : EntityValidator<ByFarmCriterion> {
    override suspend fun onUpdate(value: ByFarmCriterion) {
        validate(value) {
            val organizationId = requireNotNull(getAuthentication().currentOrganizationId)
            val acl = acl.findByOrganizationAndFarm(organizationId, value.farmId)

            validate(ByFarmCriterion::farmId).validate(IsPresent) { acl != null }
            validate(ByFarmCriterion::fieldIds).validate(IsPresent) {
                if (acl?.fieldIds == null || value.fieldIds == null) true else {
                    val requiredFieldIds = requireNotNull(value.fieldIds).toSet()
                    val providedFieldIds = requireNotNull(acl.fieldIds).toSet()
                    requiredFieldIds.subtract(providedFieldIds).isEmpty()
                }
            }
        }
    }
}
