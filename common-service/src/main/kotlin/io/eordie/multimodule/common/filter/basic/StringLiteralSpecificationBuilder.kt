package io.eordie.multimodule.common.filter.basic

import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import org.babyfish.jimmer.sql.ast.LikeMode
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.`ilike?`
import org.babyfish.jimmer.sql.kt.ast.expression.like
import org.babyfish.jimmer.sql.kt.ast.expression.not

class StringLiteralSpecificationBuilder : ComparableLiteralSpecificationBuilder<StringLiteralFilter, String>() {

    override fun invoke(
        filter: StringLiteralFilter,
        path: KExpression<String>
    ): List<KNonNullExpression<Boolean>> {
        val secondary = listOfNotNull(
            filter.like?.let { path.`ilike?`(it, LikeMode.ANYWHERE) },
            filter.nlike?.let { path.like(it, LikeMode.ANYWHERE).not() },
            filter.startsWith?.let { path.`ilike?`(it, LikeMode.START) },
            filter.endsWith?.let { path.`ilike?`(it, LikeMode.END) }
        )

        return super.invoke(filter, path) + secondary
    }
}
