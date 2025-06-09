package io.eordie.multimodule.organization.management.events

import io.eordie.multimodule.common.repository.event.EventListener
import io.eordie.multimodule.common.security.context.withSystemContext
import io.eordie.multimodule.contracts.basic.event.MutationEvent
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.organization.models.acl.FarmAcl
import io.eordie.multimodule.organization.management.repository.OrganizationDepartmentFactory
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.Topic
import jakarta.inject.Singleton

@Singleton
@KafkaListener
class FarmAclEventListener(
    private val departments: OrganizationDepartmentFactory
) : EventListener<FarmAcl> {

    @Topic("farm_acl")
    override suspend fun onEvent(
        causedBy: AuthenticationDetails?,
        event: MutationEvent<FarmAcl>
    ) {
        withSystemContext {
            if (event.isDeleted()) {
                departments.deleteByFarmId(event.getPrevious().farmId)
            } else if (event.isUpdated() && event.hasChangesOnAny(FarmAcl::fieldIds)) {
                val actual = event.getActual()
                val fieldIds = actual.fieldIds
                if (fieldIds != null) {
                    departments.updateFarmCriteria(actual.organizationId, actual.farmId, fieldIds)
                }
            }
        }
    }
}
