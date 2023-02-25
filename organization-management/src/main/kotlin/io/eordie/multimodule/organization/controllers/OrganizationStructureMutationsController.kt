package io.eordie.multimodule.organization.controllers

import io.eordie.multimodule.example.contracts.basic.exception.ValidationError
import io.eordie.multimodule.example.contracts.basic.exception.ValidationException
import io.eordie.multimodule.example.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPositionFilter
import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPositionInput
import io.eordie.multimodule.example.contracts.organization.services.OrganizationStructureMutations
import io.eordie.multimodule.organization.models.OrganizationPositionModelDraft
import io.eordie.multimodule.organization.models.parentId
import io.eordie.multimodule.organization.repository.OrganizationFactory
import io.eordie.multimodule.organization.repository.OrganizationPositionFactory
import io.eordie.multimodule.organization.repository.OrganizationPositionsRepository
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.toSet
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import java.util.*

@Singleton
class OrganizationStructureMutationsController(
    private val organizations: OrganizationFactory,
    private val positions: OrganizationPositionFactory,
    private val positionsRepository: OrganizationPositionsRepository
) : OrganizationStructureMutations {

    private fun cycleDetected(): Nothing = throw ValidationException(
        listOf(ValidationError(".parentId", "cycle detected between parent and child"))
    )

    private fun roleNotFound(role: String): Nothing = throw ValidationException(
        listOf(ValidationError(".roles", "role with name '$role' not found'"))
    )

    override suspend fun position(
        currentOrganization: CurrentOrganization,
        position: OrganizationPositionInput
    ): OrganizationPosition {
        val organization = organizations.getOrganizationWithRoles(currentOrganization.id)
        return positions.save<OrganizationPositionModelDraft>(position.id) { _, value ->
            val roleIndex = organization.roles.associateBy { it.name }

            value.deleted = false
            value.parentId = position.parentId
            value.roles = position.roles.map { roleIndex[it] ?: roleNotFound(it) }
            value.name = position.name
        }.convert()
    }

    override suspend fun deletePosition(positionId: UUID): Boolean {
        return positions.deletePosition(positionId)
    }

    override suspend fun changePositionsParent(previousParentId: UUID, newParentId: UUID?): Boolean {
        if (newParentId != null) {
            val parentIds = positionsRepository.getParentIds(newParentId)
            positions.findIdsBySpecification {
                where(table.parentId eq previousParentId)
            }.collect {
                if (parentIds.contains(it)) {
                    cycleDetected()
                }
            }
        }
        return positions.changeParent(previousParentId, newParentId)
    }

    override suspend fun internalTruncate(filter: OrganizationPositionFilter): Int =
        positions.truncateByIds(positions.findIdsByFilter(filter).toSet())
}
