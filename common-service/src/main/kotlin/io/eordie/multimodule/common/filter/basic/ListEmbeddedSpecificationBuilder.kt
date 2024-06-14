package io.eordie.multimodule.common.filter.basic

import io.eordie.multimodule.common.filter.SpecificationBuilder
import io.eordie.multimodule.common.repository.ext.contains
import io.eordie.multimodule.common.repository.ext.notContains
import io.eordie.multimodule.common.repository.ext.overlap
import io.eordie.multimodule.contracts.basic.filters.LiteralFilter
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.babyfish.jimmer.sql.kt.ast.expression.not

interface ListEmbeddedSpecificationBuilder<F : LiteralFilter<T>, T : Any> : SpecificationBuilder<F, List<T>> {

    fun List<T>.asTypedArray(): Array<T>

    override fun invoke(filter: F, path: KExpression<List<T>>): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            filter.eq?.let { path.contains(it) },
            filter.ne?.let { path.notContains(it) },
            filter.of?.let { path.overlap(it.asTypedArray()) },
            filter.nof?.let { path.overlap(it.asTypedArray()).not() },
            filter.exists?.let { if (it) path.isNotNull() else path.isNull() }
        )
    }
}
