@file:Suppress("TooManyFunctions")

package io.eordie.multimodule.common.repository.ext

import io.eordie.multimodule.contracts.utils.safeCast
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.and
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.expression.sql
import org.babyfish.jimmer.sql.kt.ast.expression.value

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

fun <T : Any> KExpression<List<T>>.intersection(values: Array<T>): KNonNullExpression<List<T>> {
    val expression = this
    return safeCast(sql(List::class, "array_intersect(%e, array[%v])") {
        value(values)
        expression(expression)
    })
}

fun <T : Any> KExpression<List<T>>.overlap(values: Array<T>): KNonNullExpression<Boolean> {
    val expression = this
    return sql(Boolean::class, "%e && array[%v]") {
        value(values)
        expression(expression)
    }
}

val <T : Any> KExpression<List<T>>.arraySize: KNonNullExpression<Int>
    get() = run {
        val expression = this
        return sql(Int::class, "array_length(%e, 1)") {
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

fun KNonNullExpression<Boolean>.asList(): List<KNonNullExpression<Boolean>> = listOf(this)

fun List<KNonNullExpression<Boolean>>.or(): KNonNullExpression<Boolean> =
    if (this.isEmpty()) value(true) else requireNotNull(or(*this.toTypedArray()))

fun List<KNonNullExpression<Boolean>?>?.and(): KNonNullExpression<Boolean>? =
    if (this.isNullOrEmpty()) null else and(*this.toTypedArray())

inline fun <reified T : Any> KExpression<T>.arrayAgg(
    distinct: Boolean = true,
    filterNulls: Boolean = true
): KNonNullExpression<out Array<T>> {
    val property = this
    val type = arrayOf<T>()::class
    val withDistinct = if (distinct) "distinct" else ""
    val withFilter = if (filterNulls) "filter (where %e is not null)" else ""
    return sql(type, "array_agg($withDistinct %e) $withFilter") {
        if (filterNulls) expression(property)
        expression(property)
    }
}
