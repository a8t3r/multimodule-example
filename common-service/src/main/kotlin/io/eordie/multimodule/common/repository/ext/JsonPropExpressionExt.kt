package io.eordie.multimodule.common.repository.ext

import org.babyfish.jimmer.sql.kt.ast.expression.KNullableExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.sqlNullable
import kotlin.reflect.KClass

fun KPropExpression<out Any>.jsonStr(path: String): KNullableExpression<String> = this.json(path, String::class)

fun <T : Any> KPropExpression<out Any>.json(path: String, chainType: KClass<T>): KNullableExpression<T> {
    val expression = this
    return sqlNullable(chainType, "(%e->>%v)") {
        expression(expression)
        value(path)
    }
}
