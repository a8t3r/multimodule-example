package io.eordie.multimodule.example.filter.basic

import io.eordie.multimodule.example.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.example.repository.arrayLike
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.not

class StringListSpecificationBuilder : ListEmbeddedSpecificationBuilder<StringLiteralFilter, String> {
    override fun List<String>.asTypedArray(): Array<String> = this.toTypedArray()

    override fun invoke(
        filter: StringLiteralFilter,
        path: KPropExpression<List<String>>
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
