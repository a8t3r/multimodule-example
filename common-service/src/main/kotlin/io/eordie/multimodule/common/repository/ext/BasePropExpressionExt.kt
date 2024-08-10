package io.eordie.multimodule.common.repository.ext

import org.babyfish.jimmer.sql.ast.impl.table.TableImplementor
import org.babyfish.jimmer.sql.ast.table.spi.PropExpressionImplementor
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.not

fun <T : Any> KExpression<T>.name(): String {
    val propImplementor = this as PropExpressionImplementor<*>
    val tableImplementor = propImplementor.table as TableImplementor<*>
    val property = propImplementor.prop
    return if (tableImplementor.joinProp == null) property.name else {
        "${tableImplementor.joinProp.name}.${property.name}"
    }
}

fun KNonNullExpression<Boolean>?.negateUnless(condition: Boolean?): KNonNullExpression<Boolean>? {
    return if (this == null || condition != false) this else this.not()
}
