package io.eordie.multimodule.contracts.basic.filters

import io.eordie.multimodule.contracts.utils.OffsetDateTimeStr
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.time.temporal.Temporal

/**
 * https://www.gatsbyjs.com/docs/graphql-reference/#complete-list-of-possible-operators
 */
interface ComparableFilter<T : Comparable<T>> : LiteralFilter<T> {
    val gt: T?
    val gte: T?
    val lt: T?
    val lte: T?
}

interface NumericFilter<T> : ComparableFilter<T> where T : Number, T : Comparable<T>
interface TemporalFilter<T> : ComparableFilter<T> where T : Temporal, T : Comparable<T>
interface EnumLiteralFilter<T> : LiteralFilter<T> where T : Enum<T>

@Serializable
data class IntNumericFilter(
    override val eq: Int? = null,
    override val ne: Int? = null,
    override val of: List<Int>? = null,
    override val nof: List<Int>? = null,
    override val nil: Boolean? = null,
    override val gt: Int? = null,
    override val gte: Int? = null,
    override val lt: Int? = null,
    override val lte: Int? = null
) : NumericFilter<Int>

@Serializable
data class LongNumericFilter(
    override val eq: Long? = null,
    override val ne: Long? = null,
    override val of: List<Long>? = null,
    override val nof: List<Long>? = null,
    override val nil: Boolean? = null,
    override val gt: Long? = null,
    override val gte: Long? = null,
    override val lt: Long? = null,
    override val lte: Long? = null
) : NumericFilter<Long>

@Serializable
data class BooleanLiteralFilter(
    override val eq: Boolean? = null,
    override val ne: Boolean? = null,
    override val of: List<Boolean>? = null,
    override val nof: List<Boolean>? = null,
    override val nil: Boolean? = null,
    override val gt: Boolean? = null,
    override val gte: Boolean? = null,
    override val lt: Boolean? = null,
    override val lte: Boolean? = null
) : StringRepresentationAware<Boolean>

@Serializable
data class OffsetDateTimeLiteralFilter(
    override val eq: OffsetDateTimeStr? = null,
    override val ne: OffsetDateTimeStr? = null,
    override val of: List<OffsetDateTimeStr>? = null,
    override val nof: List<OffsetDateTimeStr>? = null,
    override val nil: Boolean? = null,
    override val gt: OffsetDateTimeStr? = null,
    override val gte: OffsetDateTimeStr? = null,
    override val lt: OffsetDateTimeStr? = null,
    override val lte: OffsetDateTimeStr? = null
) : TemporalFilter<OffsetDateTime>
