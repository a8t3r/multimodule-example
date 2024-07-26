package io.eordie.multimodule.common.filter

import io.eordie.multimodule.contracts.basic.filters.LiteralFilter
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.asNullable
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.ast.expression.isNull

interface SpecificationBuilder<F : LiteralFilter<*>, T : Any> {
    fun invoke(filter: F, path: KExpression<T>): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            filter.nil?.let {
                when {
                    it -> (if (path is KNonNullPropExpression<*>) path.asNullable() else path).isNull()
                    else -> path.isNotNull()
                }
            }
        )
    }
}
