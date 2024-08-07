package io.eordie.multimodule.organization.management.validation

import io.eordie.multimodule.common.validation.Cycle
import io.eordie.multimodule.common.validation.EntityValidator
import io.eordie.multimodule.common.validation.ensureIsAccessible
import io.eordie.multimodule.common.validation.isSimpleString
import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.contracts.organization.models.Organization
import io.eordie.multimodule.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.organization.management.models.OrganizationPositionModel
import io.eordie.multimodule.organization.management.repository.OrganizationPositionsRepository
import jakarta.inject.Singleton
import org.babyfish.jimmer.kt.isLoaded
import org.valiktor.validate
import java.util.*

@Singleton
class OrganizationPositionValidator(
    private val organizations: EntityLoader<Organization, UUID>,
    private val positions: EntityLoader<OrganizationPosition, UUID>,
    private val positionsRepository: OrganizationPositionsRepository
) : EntityValidator<OrganizationPositionModel> {

    override suspend fun onCreate(value: OrganizationPositionModel) {
        validate(value) {
            validate(OrganizationPositionModel::organizationId).ensureIsAccessible(organizations)
        }
    }

    override suspend fun onUpdate(value: OrganizationPositionModel) {
        validate(value) {
            validate(OrganizationPositionModel::name).isSimpleString()
            validate(OrganizationPositionModel::parentId).ensureIsAccessible(positions, optional = true)
            validate(OrganizationPositionModel::parentId).validate(Cycle) {
                val parentId = value.parentId
                if (parentId == null || !isLoaded(value, OrganizationPositionModel::id)) true else {
                    val parentIds = positionsRepository.getParentIds(parentId)
                    !parentIds.contains(value.id)
                }
            }
        }
    }
}
