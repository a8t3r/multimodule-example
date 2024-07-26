package io.eordie.multimodule.contracts.basic.filters

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class UUIDLiteralFilter(
    override val eq: UuidStr? = null,
    override val ne: UuidStr? = null,
    override val of: List<UuidStr>? = null,
    override val nof: List<UuidStr>? = null,
    override val nil: Boolean? = null,
    override val gt: UuidStr? = null,
    override val gte: UuidStr? = null,
    override val lt: UuidStr? = null,
    override val lte: UuidStr? = null
) : StringRepresentationAware<UuidStr>
