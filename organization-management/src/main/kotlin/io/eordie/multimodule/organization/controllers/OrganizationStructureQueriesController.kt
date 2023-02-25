package io.eordie.multimodule.organization.controllers

import io.eordie.multimodule.example.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.example.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPositionFilter
import io.eordie.multimodule.example.contracts.organization.services.OrganizationStructureQueries
import io.eordie.multimodule.organization.repository.OrganizationPositionFactory
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

@Singleton
class OrganizationStructureQueriesController(
    private val positions: OrganizationPositionFactory
) : OrganizationStructureQueries {

    override suspend fun positions(
        currentOrganization: CurrentOrganization,
        filter: OrganizationPositionFilter?
    ): List<OrganizationPosition> {
        val filterBy = (filter ?: OrganizationPositionFilter())
            .copy(organizationId = UUIDLiteralFilter(eq = currentOrganization.id))

        return positions.findAllByFilter(filterBy).map { it.convert() }.toList()
    }

    override suspend fun loadSubordinates(parentIds: List<UUID>): Map<UUID, List<OrganizationPosition>> {
        return positions.findByIdsWithSubordinates(parentIds).associateBy(
            { it.id },
            { parent -> parent.subordinates.map { it.convert() } }
        )
    }
}
