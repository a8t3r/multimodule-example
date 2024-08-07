package io.eordie.multimodule.contracts.organization.services

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequest
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequestFilter
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequestInput
import io.eordie.multimodule.contracts.utils.UuidStr

@AutoService(Query::class)
interface AccessionRequestQueries : Query {
    suspend fun accessionRequests(
        filter: AccessionRequestFilter? = null,
        pageable: Pageable? = null
    ): Page<AccessionRequest>
}

@AutoService(Query::class)
interface AccessionRequestMutations : Mutation {
    suspend fun acceptAccessionRequest(requestId: UuidStr): AccessionRequest
    suspend fun rejectAccessionRequest(requestId: UuidStr, rejectionMessage: String? = null): AccessionRequest
    suspend fun accessionRequest(input: AccessionRequestInput): AccessionRequest
    suspend fun deleteJoinRequest(requestId: UuidStr)
}
