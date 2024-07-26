package io.eordie.multimodule.contracts.basic.filters

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

/**
 * https://www.gatsbyjs.com/docs/graphql-reference/#complete-list-of-possible-operators
 */
@Introspected
@Serializable
data class StringLiteralFilter(
    override val eq: String? = null,
    override val ne: String? = null,
    override val of: List<String>? = null,
    override val nof: List<String>? = null,
    override val nil: Boolean? = null,
    override val gt: String? = null,
    override val gte: String? = null,
    override val lt: String? = null,
    override val lte: String? = null,
    val like: String? = null,
    val nlike: String? = null,
    val startsWith: String? = null,
    val endsWith: String? = null
) : ComparableFilter<String>
