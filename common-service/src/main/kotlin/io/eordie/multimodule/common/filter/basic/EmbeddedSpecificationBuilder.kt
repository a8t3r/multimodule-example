package io.eordie.multimodule.common.filter.basic

import io.eordie.multimodule.common.filter.SpecificationBuilder
import io.eordie.multimodule.contracts.basic.filters.LiteralFilter
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.ne
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.expression.valueNotIn
import java.util.function.Predicate

open class EmbeddedSpecificationBuilder<F : LiteralFilter<T>, T : Any> : SpecificationBuilder<F, T> {

    override fun invoke(filter: F, path: KExpression<T>): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            filter.eq?.let { path eq it },
            filter.ne?.let { path ne it },
            filter.of?.let { path valueIn it },
            filter.nof?.let { path valueNotIn it }
        ) + super.invoke(filter, path)
    }

    override fun predicates(filter: F): List<Predicate<T?>> = super.predicates(filter) + listOfNotNull(
        filter.eq?.let { f -> Predicate { it == f } },
        filter.ne?.let { f -> Predicate { it != f } },
        filter.of?.let { f -> Predicate { it in f } },
        filter.nof?.let { f -> Predicate { it !in f } }
    )
}
