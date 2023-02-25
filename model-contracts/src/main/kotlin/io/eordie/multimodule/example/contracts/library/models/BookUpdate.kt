package io.eordie.multimodule.example.contracts.library.models

import io.eordie.multimodule.example.contracts.InputOnly
import io.eordie.multimodule.example.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@InputOnly
@Introspected
@Serializable
data class BookUpdate(
    val id: UuidStr,
    val name: String? = null,
    val authorIds: List<UuidStr>? = null
)
