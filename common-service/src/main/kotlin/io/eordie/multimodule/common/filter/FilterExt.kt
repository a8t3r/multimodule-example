package io.eordie.multimodule.common.filter

import io.eordie.multimodule.common.filter.basic.ComparableListSpecificationBuilder
import io.eordie.multimodule.common.filter.basic.ComparableLiteralSpecificationBuilder
import io.eordie.multimodule.common.filter.basic.EmbeddedSpecificationBuilder
import io.eordie.multimodule.common.filter.basic.ListEmbeddedSpecificationBuilder
import io.eordie.multimodule.common.filter.basic.StringListSpecificationBuilder
import io.eordie.multimodule.common.filter.basic.StringLiteralSpecificationBuilder
import io.eordie.multimodule.common.repository.ext.and
import io.eordie.multimodule.common.utils.GenericTypes
import io.eordie.multimodule.contracts.basic.filters.ComparableFilter
import io.eordie.multimodule.contracts.basic.filters.LiteralFilter
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.utils.safeCast
import io.eordie.multimodule.contracts.utils.uncheckedCast
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression

infix fun <F : LiteralFilter<T>, T : Any> KExpression<List<T>>.acceptMany(
    filter: F?
): KNonNullExpression<Boolean>? {
    return if (filter == null) null else {
        dispatchMany(filter).invoke(filter, this).and()
    }
}

infix fun <F : LiteralFilter<T>, T : Any> KExpression<T>.accept(filter: F?): KNonNullExpression<Boolean>? {
    return if (filter == null) null else {
        dispatchSingle(filter).invoke(filter, this).and()
    }
}

infix fun <F : LiteralFilter<T>, T : Any> F?.matches(value: T?): Boolean {
    val filter = this
    return if (filter == null) true else {
        dispatchSingle(filter).predicates(filter).all { it.test(value) }
    }
}

infix fun <F : LiteralFilter<T>, T : Any> F?.anyMatches(values: List<T>?): Boolean {
    val filter = this
    return if (filter == null) true else {
        dispatchSingle(filter).predicates(filter).all { predicate ->
            values?.any { predicate.test(it) } ?: predicate.test(null)
        }
    }
}

private fun <F : LiteralFilter<T>, T : Any> dispatchSingle(filter: F): SpecificationBuilder<F, T> {
    return safeCast(
        when (filter) {
            is StringLiteralFilter -> StringLiteralSpecificationBuilder()
            is ComparableFilter<*> -> ComparableLiteralSpecificationBuilder()
            else -> EmbeddedSpecificationBuilder()
        }
    )
}

private fun <F : LiteralFilter<T>, T : Any> dispatchMany(filter: F): ListEmbeddedSpecificationBuilder<F, T> {
    return when (filter) {
        is StringLiteralFilter -> safeCast(StringListSpecificationBuilder())
        is ComparableFilter<*> -> {
            fun <R : Comparable<R>> typeArgument(filter: ComparableFilter<R>): Class<R> {
                return GenericTypes.getTypeArgument(filter, ComparableFilter::class).java.uncheckedCast()
            }
            safeCast(ComparableListSpecificationBuilder(typeArgument(filter)))
        }

        else -> error("unknown filter type: ${filter::class.simpleName}")
    }
}
