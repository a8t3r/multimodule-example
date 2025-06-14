package io.eordie.multimodule.common.filter.basic

import io.eordie.multimodule.contracts.basic.filters.ComparableFilter
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.ge
import org.babyfish.jimmer.sql.kt.ast.expression.gt
import org.babyfish.jimmer.sql.kt.ast.expression.le
import org.babyfish.jimmer.sql.kt.ast.expression.lt
import java.util.function.Predicate

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

    override fun predicates(filter: F): List<Predicate<T?>> = super.predicates(filter) + listOfNotNull(
        filter.gt?.let { f -> Predicate { it != null && it > f } },
        filter.gte?.let { f -> Predicate { it != null && it >= f } },
        filter.lt?.let { f -> Predicate { it != null && it < f } },
        filter.lte?.let { f -> Predicate { it != null && it <= f } }
    )
}
