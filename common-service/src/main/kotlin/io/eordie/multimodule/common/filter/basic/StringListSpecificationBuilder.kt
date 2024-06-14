package io.eordie.multimodule.common.filter.basic

import io.eordie.multimodule.common.repository.ext.arrayLike
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.not

class StringListSpecificationBuilder : ListEmbeddedSpecificationBuilder<StringLiteralFilter, String> {
    override fun List<String>.asTypedArray(): Array<String> = this.toTypedArray()

    override fun invoke(
        filter: StringLiteralFilter,
        path: KExpression<List<String>>
    ): List<KNonNullExpression<Boolean>> {
        val secondary = listOfNotNull(
            filter.like?.let { path.arrayLike("%$it%") },
            filter.nlike?.let { path.arrayLike("%$it%").not() },
            filter.startsWith?.let { path.arrayLike("$it%") },
            filter.endsWith?.let { path.arrayLike("%$it") }
        )

        return super.invoke(filter, path) + secondary
    }
}
