package io.eordie.multimodule.example.filter

import io.eordie.multimodule.example.contracts.basic.filters.LiteralFilter
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression

interface SpecificationBuilder<F : LiteralFilter<*>, T : Any> {
    fun invoke(filter: F, path: KPropExpression<T>): List<KNonNullExpression<Boolean>>
}
