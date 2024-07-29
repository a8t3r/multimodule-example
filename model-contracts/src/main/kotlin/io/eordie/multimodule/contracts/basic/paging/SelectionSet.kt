package io.eordie.multimodule.contracts.basic.paging

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class SelectionSet(
    val fields: List<String>
)
