package io.eordie.multimodule.example.filter.basic

import io.eordie.multimodule.example.contracts.basic.filters.LiteralFilter
import io.eordie.multimodule.example.filter.SpecificationBuilder
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.asNullable
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.babyfish.jimmer.sql.kt.ast.expression.ne
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.expression.valueNotIn

open class EmbeddedSpecificationBuilder<F : LiteralFilter<T>, T : Any> : SpecificationBuilder<F, T> {

    override fun invoke(filter: F, path: KPropExpression<T>): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            filter.eq?.let { path.eq(it) },
            filter.ne?.let { path.ne(it) },
            filter.of?.let { path.valueIn(it) },
            filter.nof?.let { path.valueNotIn(it) },
            filter.exists?.let {
                when {
                    it -> path.isNotNull()
                    path is KNonNullPropExpression<*> -> path.asNullable().isNull()
                    else -> path.isNull()
                }
            }
        )
    }
}
