package io.eordie.multimodule.contracts.basic.filters

import io.eordie.multimodule.contracts.utils.OffsetDateTimeStr
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.time.temporal.Temporal

/**
 * https://www.gatsbyjs.com/docs/graphql-reference/#complete-list-of-possible-operators
 */
interface ComparableFilter<T : Comparable<T>> : LiteralFilter<T> {
    var gt: T?
    var gte: T?
    var lt: T?
    var lte: T?
}

interface NumericFilter<T> : ComparableFilter<T> where T : Number, T : Comparable<T>
interface TemporalFilter<T> : ComparableFilter<T> where T : Temporal, T : Comparable<T>

@Serializable
data class IntNumericFilter(
    override var eq: Int? = null,
    override var ne: Int? = null,
    override var of: List<Int>? = null,
    override var nof: List<Int>? = null,
    override var exists: Boolean? = null,
    override var gt: Int? = null,
    override var gte: Int? = null,
    override var lt: Int? = null,
    override var lte: Int? = null
) : NumericFilter<Int>

@Serializable
data class LongNumericFilter(
    override var eq: Long? = null,
    override var ne: Long? = null,
    override var of: List<Long>? = null,
    override var nof: List<Long>? = null,
    override var exists: Boolean? = null,
    override var gt: Long? = null,
    override var gte: Long? = null,
    override var lt: Long? = null,
    override var lte: Long? = null
) : NumericFilter<Long>

@Serializable
data class BooleanLiteralFilter(
    override var eq: Boolean? = null,
    override var ne: Boolean? = null,
    override var of: List<Boolean>? = null,
    override var nof: List<Boolean>? = null,
    override var exists: Boolean? = null,
    override var gt: Boolean? = null,
    override var gte: Boolean? = null,
    override var lt: Boolean? = null,
    override var lte: Boolean? = null
) : StringRepresentationAware<Boolean>

@Serializable
data class OffsetDateTimeLiteralFilter(
    override var eq: OffsetDateTimeStr? = null,
    override var ne: OffsetDateTimeStr? = null,
    override var of: List<OffsetDateTimeStr>? = null,
    override var nof: List<OffsetDateTimeStr>? = null,
    override var exists: Boolean? = null,
    override var gt: OffsetDateTimeStr? = null,
    override var gte: OffsetDateTimeStr? = null,
    override var lt: OffsetDateTimeStr? = null,
    override var lte: OffsetDateTimeStr? = null
) : TemporalFilter<OffsetDateTime>
