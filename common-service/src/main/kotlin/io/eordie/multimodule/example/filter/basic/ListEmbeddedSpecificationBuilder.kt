package io.eordie.multimodule.example.filter.basic

import io.eordie.multimodule.example.contracts.basic.filters.LiteralFilter
import io.eordie.multimodule.example.filter.SpecificationBuilder
import io.eordie.multimodule.example.repository.contains
import io.eordie.multimodule.example.repository.notContains
import io.eordie.multimodule.example.repository.overlap
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.babyfish.jimmer.sql.kt.ast.expression.not

interface ListEmbeddedSpecificationBuilder<F : LiteralFilter<T>, T : Any> : SpecificationBuilder<F, List<T>> {

    fun List<T>.asTypedArray(): Array<T>

    override fun invoke(filter: F, path: KPropExpression<List<T>>): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            filter.eq?.let { path.contains(it) },
            filter.ne?.let { path.notContains(it) },
            filter.of?.let { path.overlap(it.asTypedArray()) },
            filter.nof?.let { path.overlap(it.asTypedArray()).not() },
            filter.exists?.let { if (it) path.isNotNull() else path.isNull() }
        )
    }
}
