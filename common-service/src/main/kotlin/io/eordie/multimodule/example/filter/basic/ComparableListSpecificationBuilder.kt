package io.eordie.multimodule.example.filter.basic

import io.eordie.multimodule.example.contracts.basic.filters.ComparableFilter
import io.eordie.multimodule.example.repository.ge
import io.eordie.multimodule.example.repository.gt
import io.eordie.multimodule.example.repository.le
import io.eordie.multimodule.example.repository.lt
import io.micronaut.core.util.ArrayUtils
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression

class ComparableListSpecificationBuilder<F : ComparableFilter<T>, T : Comparable<T>>(
    private val numberClass: Class<T>
) : ListEmbeddedSpecificationBuilder<F, T> {

    override fun List<T>.asTypedArray(): Array<T> = ArrayUtils.toArray(this, numberClass)

    override fun invoke(filter: F, path: KPropExpression<List<T>>): List<KNonNullExpression<Boolean>> {
        val secondary = listOfNotNull(
            filter.gt?.let { path.gt(it) },
            filter.gte?.let { path.ge(it) },
            filter.lt?.let { path.lt(it) },
            filter.lte?.let { path.le(it) }
        )

        return super.invoke(filter, path) + secondary
    }
}
