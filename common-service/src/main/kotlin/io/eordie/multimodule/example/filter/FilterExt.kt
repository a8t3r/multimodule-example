package io.eordie.multimodule.example.filter

import io.eordie.multimodule.example.contracts.basic.filters.ComparableFilter
import io.eordie.multimodule.example.contracts.basic.filters.LiteralFilter
import io.eordie.multimodule.example.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.example.filter.basic.ComparableListSpecificationBuilder
import io.eordie.multimodule.example.filter.basic.ComparableLiteralSpecificationBuilder
import io.eordie.multimodule.example.filter.basic.ListEmbeddedSpecificationBuilder
import io.eordie.multimodule.example.filter.basic.StringListSpecificationBuilder
import io.eordie.multimodule.example.filter.basic.StringLiteralSpecificationBuilder
import io.eordie.multimodule.example.repository.and
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.javaType

fun <F : LiteralFilter<T>, T : Any> KPropExpression<List<T>>.acceptMany(filter: F?): KNonNullExpression<Boolean>? {
    return if (filter == null) null else {
        dispatchMany(filter).invoke(filter, this).and()
    }
}

fun <F : LiteralFilter<T>, T : Any> KPropExpression<T>.accept(filter: F?): KNonNullExpression<Boolean>? {
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

private val cache = mutableMapOf<KClass<*>, Type>()

@Suppress("UNCHECKED_CAST")
private fun <F : LiteralFilter<T>, T : Any> dispatchMany(filter: F): ListEmbeddedSpecificationBuilder<F, T> {
    return when (filter) {
        is StringLiteralFilter -> StringListSpecificationBuilder() as ListEmbeddedSpecificationBuilder<F, T>
        is ComparableFilter<*> -> {
            @OptIn(ExperimentalStdlibApi::class)
            @Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER")
            fun <R> typeArgument(): Class<R> where R : T, R : Comparable<T> {
                return cache.getOrPut(filter::class) {
                    requireNotNull(
                        filter::class.allSupertypes
                            .first { it.classifier == ComparableFilter::class }
                            .arguments[0].type?.javaType
                    )
                } as Class<R>
            }
            ComparableListSpecificationBuilder(typeArgument()) as ListEmbeddedSpecificationBuilder<F, T>
        }
        else -> error("unknown filter type: ${filter::class.simpleName}")
    }
}
