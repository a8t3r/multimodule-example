package io.eordie.multimodule.common.filter.basic

import io.eordie.multimodule.common.repository.ext.ge
import io.eordie.multimodule.common.repository.ext.gt
import io.eordie.multimodule.common.repository.ext.le
import io.eordie.multimodule.common.repository.ext.lt
import io.eordie.multimodule.contracts.basic.filters.ComparableFilter
import io.micronaut.core.util.ArrayUtils
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression

class ComparableListSpecificationBuilder<F : ComparableFilter<T>, T : Comparable<T>>(
    private val comparableClass: Class<T>
) : ListEmbeddedSpecificationBuilder<F, T> {

    override fun List<T>.asTypedArray(): Array<T> = ArrayUtils.toArray(this, comparableClass)

    override fun invoke(filter: F, path: KExpression<List<T>>): List<KNonNullExpression<Boolean>> {
        val secondary = listOfNotNull(
            filter.gt?.let { path gt it },
            filter.gte?.let { path ge it },
            filter.lt?.let { path lt it },
            filter.lte?.let { path le it }
        )

        return super.invoke(filter, path) + secondary
    }
}
