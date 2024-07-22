package io.eordie.multimodule.common.filter.basic

import io.eordie.multimodule.common.filter.SpecificationBuilder
import io.eordie.multimodule.contracts.basic.filters.LiteralFilter
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.asNullable
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.babyfish.jimmer.sql.kt.ast.expression.ne
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.expression.valueNotIn

open class EmbeddedSpecificationBuilder<F : LiteralFilter<T>, T : Any> : SpecificationBuilder<F, T> {

    override fun invoke(filter: F, path: KExpression<T>): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            filter.eq?.let { path.eq(it) },
            filter.ne?.let { path.ne(it) },
            filter.of?.let { path.valueIn(it) },
            filter.nof?.let { path.valueNotIn(it) },
            filter.nil?.let {
                when {
                    it -> (if (path is KNonNullPropExpression<*>) path.asNullable() else path).isNull()
                    else -> path.isNotNull()
                }
            }
        )
    }
}
