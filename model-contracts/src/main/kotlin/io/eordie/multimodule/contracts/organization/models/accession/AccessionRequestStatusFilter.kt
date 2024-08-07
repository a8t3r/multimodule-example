package io.eordie.multimodule.contracts.organization.models.accession

import io.eordie.multimodule.contracts.basic.filters.EnumLiteralFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class AccessionRequestStatusFilter(
    override val eq: AccessionRequestStatus?,
    override val ne: AccessionRequestStatus?,
    override val of: List<AccessionRequestStatus>?,
    override val nof: List<AccessionRequestStatus>?,
    override val nil: Boolean?
) : EnumLiteralFilter<AccessionRequestStatus>
