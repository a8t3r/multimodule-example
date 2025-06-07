package io.eordie.multimodule.common.filter.basic

import io.eordie.multimodule.contracts.basic.filters.ComparableFilter
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.ge
import org.babyfish.jimmer.sql.kt.ast.expression.gt
import org.babyfish.jimmer.sql.kt.ast.expression.le
import org.babyfish.jimmer.sql.kt.ast.expression.lt

open class ComparableLiteralSpecificationBuilder<F : ComparableFilter<T>, T : Comparable<T>> :
    EmbeddedSpecificationBuilder<F, T>() {

    override fun invoke(filter: F, path: KExpression<T>): List<KNonNullExpression<Boolean>> {
        val secondary = listOfNotNull(
            filter.gt?.let { path gt it },
            filter.gte?.let { path ge it },
            filter.lt?.let { path lt it },
            filter.lte?.let { path le it }
        )

        return super.invoke(filter, path) + secondary
    }
}
