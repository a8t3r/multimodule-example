package io.eordie.multimodule.contracts.basic.filters

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import io.eordie.multimodule.contracts.utils.OffsetDateTimeStr
import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.time.temporal.Temporal

@Introspected
@Serializable
abstract class LiteralFilter<T> {
    var eq: T? = null
    var ne: T? = null
    var of: List<T>? = null
    var nof: List<T>? = null
    var nil: Boolean? = null
}

/**
 * https://www.gatsbyjs.com/docs/graphql-reference/#complete-list-of-possible-operators
 */
@Serializable
abstract class ComparableFilter<T : Comparable<T>> : LiteralFilter<T>() {
    var gt: T? = null
    var gte: T? = null
    var lt: T? = null
    var lte: T? = null

    @GraphQLIgnore
    fun transformToStringLiteral(): StringLiteralFilter {
        return StringLiteralFilter().also { f ->
            f.eq = eq?.toString()
            f.ne = ne?.toString()
            f.of = of?.map { it.toString() }
            f.nof = nof?.map { it.toString() }
            f.nil = nil
            f.gt = gt?.toString()
            f.gte = gte?.toString()
            f.lt = lt?.toString()
            f.lte = lte?.toString()
        }
    }
}

@Introspected
@Serializable
class StringLiteralFilter() : ComparableFilter<String>() {
    var like: String? = null
    var nlike: String? = null
    var startsWith: String? = null
    var endsWith: String? = null

    constructor(block: StringLiteralFilter.() -> Unit) : this() { apply(block) }
}

@Introspected
@Serializable
sealed class NumericFilter<T> : ComparableFilter<T>() where T : Number, T : Comparable<T>

@Introspected
@Serializable
sealed class TemporalFilter<T> : ComparableFilter<T>() where T : Temporal, T : Comparable<T>

@Introspected
@Serializable
class EnumLiteralFilter<T>() : LiteralFilter<T>() where T : Enum<T> {
    constructor(block: EnumLiteralFilter<T>.() -> Unit) : this() { apply(block) }
}

@Introspected
@Serializable
class IntNumericFilter() : NumericFilter<Int>() {
    constructor(block: IntNumericFilter.() -> Unit) : this() { apply(block) }
}

@Introspected
@Serializable
class LongNumericFilter() : NumericFilter<Long>() {
    constructor(block: LongNumericFilter.() -> Unit) : this() { apply(block) }
}

@Introspected
@Serializable
class DoubleNumericFilter() : NumericFilter<Double>() {
    constructor(block: DoubleNumericFilter.() -> Unit) : this() { apply(block) }
}

@Introspected
@Serializable
class BooleanLiteralFilter() : ComparableFilter<Boolean>() {
    constructor(block: BooleanLiteralFilter.() -> Unit) : this() { apply(block) }
}

@Introspected
@Serializable
class OffsetDateTimeLiteralFilter() : TemporalFilter<OffsetDateTimeStr>() {
    constructor(block: OffsetDateTimeLiteralFilter.() -> Unit) : this() { apply(block) }
}

@Introspected
@Serializable
class UUIDLiteralFilter() : ComparableFilter<UuidStr>() {
    constructor(block: UUIDLiteralFilter.() -> Unit) : this() { apply(block) }
}
