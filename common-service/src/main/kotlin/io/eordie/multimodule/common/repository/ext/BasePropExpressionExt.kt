package io.eordie.multimodule.common.repository.ext

import org.babyfish.jimmer.sql.ast.impl.table.TableImplementor
import org.babyfish.jimmer.sql.ast.table.spi.PropExpressionImplementor
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.sql

fun <T : Any> KExpression<T>.name(): String {
    val propImplementor = this as PropExpressionImplementor<*>
    val tableImplementor = propImplementor.table as TableImplementor<*>
    val property = propImplementor.prop
    return if (tableImplementor.joinProp == null) property.name else {
        "${tableImplementor.joinProp.name}.${property.name}"
    }
}

private fun <T : Any> KExpression<List<T>>.operator(value: T, name: String): KNonNullExpression<Boolean> {
    val expression = this
    return sql(Boolean::class, "%v $name ANY(%e)") {
        value(value)
        expression(expression)
    }
}

fun <T : Any> KExpression<List<T>>.contains(value: T) = operator(value, "=")
fun <T : Any> KExpression<List<T>>.notContains(value: T) = operator(value, "!=")
fun <T : Any> KExpression<List<T>>.gt(value: T) = operator(value, "<")
fun <T : Any> KExpression<List<T>>.ge(value: T) = operator(value, "<=")
fun <T : Any> KExpression<List<T>>.lt(value: T) = operator(value, ">")
fun <T : Any> KExpression<List<T>>.le(value: T) = operator(value, ">=")

inline fun <reified V : Any> KExpression<*>.cast(): KNonNullExpression<V> {
    val expression = this
    return sql(V::class, "cast(%e as ${V::class.simpleName})") {
        expression(expression)
    }
}

fun <T : Any> KExpression<List<T>>.overlap(values: Array<T>): KNonNullExpression<Boolean> {
    val expression = this
    return sql(Boolean::class, "%e && array[%v]") {
        value(values)
        expression(expression)
    }
}

fun <T : Any> KExpression<List<T>>.arrayLike(pattern: String): KNonNullExpression<Boolean> {
    val expression = this
    return sql(Boolean::class, "(array_to_string(%e, ',') ilike %v or array_to_string(%e, ',') ilike ',' || %v)") {
        expression(expression)
        value(pattern)
    }
}

fun List<KNonNullExpression<Boolean>?>?.and(): KNonNullExpression<Boolean>? {
    return if (this.isNullOrEmpty()) null else {
        org.babyfish.jimmer.sql.kt.ast.expression.and(*this.toTypedArray())
    }
}

fun <T : Any> KExpression<T>.arrayAgg(distinct: Boolean = true): KNonNullExpression<out List<T>> {
    val property = this
    return sql(emptyList<T>()::class, "array_agg(${if (distinct) "distinct" else ""} %e)") {
        expression(property)
    }
}
