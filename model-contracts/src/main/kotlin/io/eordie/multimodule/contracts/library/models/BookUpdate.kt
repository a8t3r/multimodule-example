package io.eordie.multimodule.contracts.library.models

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class BookUpdate(
    val id: UuidStr,
    val name: String? = null,
    val authorIds: List<UuidStr>? = null
)
