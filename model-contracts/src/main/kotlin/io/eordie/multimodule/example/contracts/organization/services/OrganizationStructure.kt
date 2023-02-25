package io.eordie.multimodule.example.contracts.organization.services

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.google.auto.service.AutoService
import io.eordie.multimodule.example.contracts.Mutation
import io.eordie.multimodule.example.contracts.Query
import io.eordie.multimodule.example.contracts.annotations.Secured
import io.eordie.multimodule.example.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPosition
import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPositionFilter
import io.eordie.multimodule.example.contracts.organization.models.structure.OrganizationPositionInput
import io.eordie.multimodule.example.contracts.utils.Roles
import java.util.*

@AutoService(Query::class)
@Secured(oneOf = [ Roles.VIEW_ORGANIZATION, Roles.VIEW_ORGANIZATIONS ])
interface OrganizationStructureQueries : Query {
    suspend fun positions(
        @GraphQLIgnore currentOrganization: CurrentOrganization,
        filter: OrganizationPositionFilter? = null
    ): List<OrganizationPosition>

    suspend fun loadSubordinates(parentIds: List<UUID>): Map<UUID, List<OrganizationPosition>>
}

@AutoService(Mutation::class)
@Secured(oneOf = [ Roles.MANAGE_ORGANIZATION, Roles.MANAGE_ORGANIZATIONS ])
interface OrganizationStructureMutations : Mutation {

    suspend fun position(
        @GraphQLIgnore currentOrganization: CurrentOrganization,
        position: OrganizationPositionInput
    ): OrganizationPosition

    suspend fun deletePosition(positionId: UUID): Boolean

    suspend fun changePositionsParent(previousParentId: UUID, newParentId: UUID?): Boolean

    suspend fun internalTruncate(filter: OrganizationPositionFilter): Int
}
