package io.eordie.multimodule.contracts.basic.filters

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

/**
 * https://www.gatsbyjs.com/docs/graphql-reference/#complete-list-of-possible-operators
 */
@Introspected
@Serializable
data class StringLiteralFilter(
    override var eq: String? = null,
    override var ne: String? = null,
    override var of: List<String>? = null,
    override var nof: List<String>? = null,
    override var exists: Boolean? = null,
    override var gt: String? = null,
    override var gte: String? = null,
    override var lt: String? = null,
    override var lte: String? = null,
    var like: String? = null,
    var nlike: String? = null,
    var startsWith: String? = null,
    var endsWith: String? = null
) : ComparableFilter<String>
