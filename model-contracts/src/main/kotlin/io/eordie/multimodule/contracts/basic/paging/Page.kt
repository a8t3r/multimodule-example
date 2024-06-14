package io.eordie.multimodule.contracts.basic.paging

import kotlinx.serialization.Serializable

enum class SortDirection {
    ASC, DESC
}

@Serializable
data class Pageable(
    val cursor: String? = null,
    val orderBy: List<SortOrder>? = null,
    val limit: Int? = null,
    val supportedOrders: Set<String>? = emptySet()
)

@Serializable
data class SortOrder(val property: String? = null, val direction: SortDirection? = null)

@Serializable
data class Page<T : Any>(val data: List<T>, val pageable: Pageable)
