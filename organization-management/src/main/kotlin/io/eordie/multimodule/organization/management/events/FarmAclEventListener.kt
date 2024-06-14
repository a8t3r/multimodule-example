package io.eordie.multimodule.organization.management.events

import io.eordie.multimodule.common.repository.event.EventListener
import io.eordie.multimodule.contracts.basic.event.MutationEvent
import io.eordie.multimodule.contracts.organization.models.acl.FarmAcl
import io.eordie.multimodule.organization.management.repository.ByFarmCriterionRepository
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.Topic
import jakarta.inject.Singleton

@Singleton
@KafkaListener
class FarmAclEventListener(
    private val repository: ByFarmCriterionRepository
) : EventListener<FarmAcl> {

    @Topic("farm_acl")
    override suspend fun onEvent(event: MutationEvent<FarmAcl>) {
        if (event.isDeleted()) {
            repository.deleteByFarmId(event.getPrevious().farmId)
        } else if (event.isUpdated() && event.hasChangesOnAny(FarmAcl::fieldIds)) {
            val actual = event.getActual()
            val fieldIds = actual.fieldIds
            if (fieldIds != null) {
                repository.updateFarmCriteria(actual.organizationId, actual.farmId, fieldIds)
            }
        }
    }
}
