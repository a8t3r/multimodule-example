package io.eordie.multimodule.contracts.basic.paging

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
enum class SortDirection {
    ASC, DESC
}

@Introspected
@Serializable
data class Pageable(
    val cursor: String? = null,
    val orderBy: List<SortOrder>? = null,
    val limit: Int? = null,
    val supportedOrders: Set<String>? = emptySet()
)

@Introspected
@Serializable
data class SortOrder(val property: String? = null, val direction: SortDirection? = null)

@Introspected
@Serializable
data class Page<T : Any>(val data: List<T>, val pageable: Pageable)
