package io.eordie.multimodule.common.filter

import io.eordie.multimodule.common.filter.basic.ComparableListSpecificationBuilder
import io.eordie.multimodule.common.filter.basic.ComparableLiteralSpecificationBuilder
import io.eordie.multimodule.common.filter.basic.ListEmbeddedSpecificationBuilder
import io.eordie.multimodule.common.filter.basic.StringListSpecificationBuilder
import io.eordie.multimodule.common.filter.basic.StringLiteralSpecificationBuilder
import io.eordie.multimodule.common.repository.ext.and
import io.eordie.multimodule.common.repository.ext.arraySize
import io.eordie.multimodule.common.utils.GenericTypes
import io.eordie.multimodule.contracts.basic.filters.ComparableFilter
import io.eordie.multimodule.contracts.basic.filters.IntNumericFilter
import io.eordie.multimodule.contracts.basic.filters.LiteralFilter
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression

fun <F : LiteralFilter<T>, T : Any> KExpression<List<T>>.acceptMany(
    filter: F?,
    arraySizeFilter: IntNumericFilter? = null
): KNonNullExpression<Boolean>? {
    val expression = this
    return buildList {
        if (filter != null) {
            addAll(dispatchMany(filter).invoke(filter, expression))
        }
        if (arraySizeFilter != null) {
            addAll(dispatchSingle(arraySizeFilter).invoke(arraySizeFilter, expression.arraySize))
        }
    }.and()
}

fun <F : LiteralFilter<T>, T : Any> KExpression<T>.accept(filter: F?): KNonNullExpression<Boolean>? {
    return if (filter == null) null else {
        dispatchSingle(filter).invoke(filter, this).and()
    }
}

@Suppress("UNCHECKED_CAST")
private fun <F : LiteralFilter<T>, T : Any> dispatchSingle(filter: F): SpecificationBuilder<F, T> {
    return when (filter) {
        is StringLiteralFilter -> StringLiteralSpecificationBuilder()
        is ComparableFilter<*> -> ComparableLiteralSpecificationBuilder()
        else -> error("unknown filter type: ${filter::class.simpleName}")
    } as SpecificationBuilder<F, T>
}

@Suppress("UNCHECKED_CAST")
private fun <F : LiteralFilter<T>, T : Any> dispatchMany(filter: F): ListEmbeddedSpecificationBuilder<F, T> {
    return when (filter) {
        is StringLiteralFilter -> StringListSpecificationBuilder() as ListEmbeddedSpecificationBuilder<F, T>
        is ComparableFilter<*> -> {
            @Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER")
            fun <R> typeArgument(): Class<R> where R : T, R : Comparable<T> {
                return GenericTypes.getTypeArgument(filter, ComparableFilter::class).java as Class<R>
            }

            ComparableListSpecificationBuilder(typeArgument()) as ListEmbeddedSpecificationBuilder<F, T>
        }
        else -> error("unknown filter type: ${filter::class.simpleName}")
    }
}
