package io.eordie.multimodule.common.filter

import io.eordie.multimodule.contracts.basic.filters.LiteralFilter
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression

interface SpecificationBuilder<F : LiteralFilter<*>, T : Any> {
    fun invoke(filter: F, path: KExpression<T>): List<KNonNullExpression<Boolean>>
}
