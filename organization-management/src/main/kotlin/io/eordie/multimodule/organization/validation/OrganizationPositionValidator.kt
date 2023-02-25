package io.eordie.multimodule.organization.validation

import io.eordie.multimodule.example.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.example.contracts.organization.models.Organization
import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.example.utils.getIfPresent
import io.eordie.multimodule.example.validation.EntityValidator
import io.eordie.multimodule.example.validation.ensureIsPresent
import io.eordie.multimodule.example.validation.isSimpleString
import io.eordie.multimodule.organization.models.OrganizationPositionModel
import io.eordie.multimodule.organization.repository.OrganizationPositionsRepository
import io.konform.validation.Validation
import jakarta.inject.Singleton
import java.util.*
import kotlin.coroutines.coroutineContext

@Singleton
class OrganizationPositionValidator(
    private val organizations: EntityLoader<Organization, UUID>,
    private val positions: EntityLoader<OrganizationPosition, UUID>,
    private val positionsRepository: OrganizationPositionsRepository
) : EntityValidator<OrganizationPositionModel> {

    override suspend fun onCreate(): Validation<OrganizationPositionModel> {
        val context = coroutineContext
        return Validation {
            OrganizationPositionModel::organizationId {
                ensureIsPresent(context, organizations)
            }
        }
    }

    override suspend fun onUpdate(): Validation<OrganizationPositionModel> {
        val context = coroutineContext
        return Validation {
            OrganizationPositionModel::name {
                isSimpleString()
            }

            OrganizationPositionModel::parentId ifPresent {
                ensureIsPresent(context, positions)
            }

            addConstraint("cycle detected between parent and child") {
                val parentId = it.parentId
                val id = it::id.getIfPresent()
                if (parentId == null || id == null) true else {
                    val parentIds = positionsRepository.getParentIds(parentId)
                    !parentIds.contains(id)
                }
            }
        }
    }
}
