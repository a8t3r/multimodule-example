package io.eordie.multimodule.contracts.regions.models

import io.eordie.multimodule.contracts.basic.filters.IntNumericFilter
import io.eordie.multimodule.contracts.basic.filters.LongNumericFilter
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class RegionsFilter(
    val parentId: LongNumericFilter? = null,
    val country: StringLiteralFilter? = null,
    val depth: IntNumericFilter? = null,
    val name: StringLiteralFilter? = null
)
