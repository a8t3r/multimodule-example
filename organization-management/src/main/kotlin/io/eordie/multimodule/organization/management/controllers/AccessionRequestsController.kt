package io.eordie.multimodule.organization.management.controllers

import io.eordie.multimodule.common.repository.EntityState
import io.eordie.multimodule.common.security.context.getAuthentication
import io.eordie.multimodule.common.security.context.withSystemContext
import io.eordie.multimodule.common.utils.convert
import io.eordie.multimodule.common.validation.error
import io.eordie.multimodule.common.validation.singleOrError
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.models.OrganizationsFilter
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequest
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequestFilter
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequestInput
import io.eordie.multimodule.contracts.organization.models.accession.AccessionRequestStatus
import io.eordie.multimodule.contracts.organization.models.extra.OrganizationPublicInformationFilter
import io.eordie.multimodule.contracts.organization.services.AccessionRequestMutations
import io.eordie.multimodule.contracts.organization.services.AccessionRequestQueries
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.orDefault
import io.eordie.multimodule.organization.management.models.AccessionRequestModel
import io.eordie.multimodule.organization.management.models.AccessionRequestModelDraft
import io.eordie.multimodule.organization.management.models.by
import io.eordie.multimodule.organization.management.repository.AccessionRequestFactory
import io.eordie.multimodule.organization.management.repository.OrganizationFactory
import io.eordie.multimodule.organization.management.validation.AccessionRequestFinalState
import io.phasetwo.client.OrganizationsResource
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher

@Singleton
class AccessionRequestsController(
    private val requests: AccessionRequestFactory,
    private val organizations: OrganizationFactory,
    private val organizationsResource: OrganizationsResource
) : AccessionRequestQueries, AccessionRequestMutations {

    private val fetcher = newFetcher(AccessionRequestModel::class).by {
        allScalarFields()
        initiatedBy()
        processedBy()
    }

    override suspend fun accessionRequests(
        filter: AccessionRequestFilter?,
        pageable: Pageable?
    ): Page<AccessionRequest> {
        return requests.findByFilter(filter.orDefault(), pageable, fetcher).convert()
    }

    override suspend fun acceptAccessionRequest(requestId: UuidStr): AccessionRequest {
        val userId = getAuthentication().userId
        val result = requests.save<AccessionRequestModelDraft>(requestId, fetcher) { state, instance ->
            checkStatus(state, instance)
            instance.processedById = userId
            instance.status = AccessionRequestStatus.ACCEPTED
        }

        organizationsResource
            .organization(result.organizationId.toString())
            .memberships()
            .add(result.initiatedById.toString())

        return result.convert()
    }

    override suspend fun rejectAccessionRequest(requestId: UuidStr, rejectionMessage: String?): AccessionRequest {
        val userId = getAuthentication().userId
        return requests.save<AccessionRequestModelDraft>(requestId, fetcher) { state, instance ->
            checkStatus(state, instance)
            instance.status = AccessionRequestStatus.REJECTED
            instance.processedById = userId
            instance.rejectionMessage = rejectionMessage
        }.convert()
    }

    override suspend fun accessionRequest(input: AccessionRequestInput): AccessionRequest {
        val organization = withSystemContext {
            organizations.findByFilter(
                OrganizationsFilter(
                    information = OrganizationPublicInformationFilter(vat = StringLiteralFilter(eq = input.vat))
                )
            ).data.singleOrError()
        }

        val userId = getAuthentication().userId
        return requests.save<AccessionRequestModelDraft>(input.id, fetcher) { state, instance ->
            checkStatus(state, instance)
            instance.vat = input.vat
            instance.initiatedById = userId
            instance.organizationId = organization.id
        }.convert()
    }

    private fun checkStatus(state: EntityState, instance: AccessionRequestModelDraft) {
        if (state.isNotExists()) {
            instance.status = AccessionRequestStatus.CREATED
        } else if (instance.status != AccessionRequestStatus.CREATED) {
            AccessionRequestFinalState.error(instance::status)
        }
    }

    override suspend fun deleteJoinRequest(requestId: UuidStr) {
        requests.deleteById(requestId)
    }
}
