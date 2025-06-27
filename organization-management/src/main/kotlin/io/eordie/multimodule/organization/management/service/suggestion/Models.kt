package io.eordie.multimodule.organization.management.service.suggestion

import com.fasterxml.jackson.annotation.JsonProperty
import io.eordie.multimodule.common.utils.JtsUtils
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.organization.models.extra.OrganizationPublicInformation

data class Address(
    @param:JsonProperty("geo_lat")
    val geoLat: Double,

    @param:JsonProperty("geo_lon")
    val geoLon: Double
)

data class AddressEnvelope(
    val data: Address,
    val value: String
)

data class Entry(
    val address: AddressEnvelope,
    val kpp: String,
    val inn: String,
    val ogrn: String
)

data class EntryEnvelope(
    val data: Entry,
    val value: String
)

data class ResultEnvelope(
    val suggestions: List<EntryEnvelope>
) {
    fun convert(): List<OrganizationPublicInformation> {
        return suggestions.map {
            OrganizationPublicInformation(
                it.value,
                it.data.kpp,
                it.data.ogrn,
                it.data.inn,
                it.data.address.value,
                TPoint(JtsUtils.WGS_84, it.data.address.data.geoLon, it.data.address.data.geoLat)
            )
        }
    }
}
