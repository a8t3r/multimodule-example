package io.eordie.multimodule.contracts.basic.filters

import io.eordie.multimodule.contracts.utils.OffsetDateTimeStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.time.temporal.Temporal

/**
 * https://www.gatsbyjs.com/docs/graphql-reference/#complete-list-of-possible-operators
 */
@Serializable
sealed interface ComparableFilter<T : Comparable<T>> : LiteralFilter<T> {
    val gt: T?
    val gte: T?
    val lt: T?
    val lte: T?
}

@Serializable
sealed interface NumericFilter<T> : ComparableFilter<T> where T : Number, T : Comparable<T>

@Serializable
sealed interface TemporalFilter<T> : ComparableFilter<T> where T : Temporal, T : Comparable<T>

@Introspected
@Serializable
data class EnumLiteralFilter<T>(
    override val eq: T? = null,
    override val ne: T? = null,
    override val of: List<T>? = null,
    override val nof: List<T>? = null,
    override val nil: Boolean? = null
) : LiteralFilter<T> where T : Enum<T>

@Introspected
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

@Introspected
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

@Introspected
@Serializable
data class DoubleNumericFilter(
    override val eq: Double? = null,
    override val ne: Double? = null,
    override val of: List<Double>? = null,
    override val nof: List<Double>? = null,
    override val nil: Boolean? = null,
    override val gt: Double? = null,
    override val gte: Double? = null,
    override val lt: Double? = null,
    override val lte: Double? = null
) : NumericFilter<Double>

@Introspected
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

@Introspected
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
