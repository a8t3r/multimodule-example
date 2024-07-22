package io.eordie.multimodule.contracts.basic.filters

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Introspected
@Serializable
data class UUIDLiteralFilter(
    override var eq: UuidStr? = null,
    override var ne: UuidStr? = null,
    override var of: List<UuidStr>? = null,
    override var nof: List<UuidStr>? = null,
    override var nil: Boolean? = null,
    override var gt: UuidStr? = null,
    override var gte: UuidStr? = null,
    override var lt: UuidStr? = null,
    override var lte: UuidStr? = null
) : StringRepresentationAware<UuidStr>
