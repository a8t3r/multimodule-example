package io.eordie.multimodule.example.library.repository

import io.eordie.multimodule.example.contracts.library.models.BooksFilter
import io.eordie.multimodule.example.filter.accept
import io.eordie.multimodule.example.filter.acceptMany
import io.eordie.multimodule.example.library.models.BookModel
import io.eordie.multimodule.example.library.models.BookModelDraft
import io.eordie.multimodule.example.library.models.authorIds
import io.eordie.multimodule.example.library.models.authors
import io.eordie.multimodule.example.library.models.id
import io.eordie.multimodule.example.library.models.name
import io.eordie.multimodule.example.library.models.updatedAt
import io.eordie.multimodule.example.repository.KBaseFactory
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*
import kotlin.coroutines.CoroutineContext

@Singleton
class BooksFactory : KBaseFactory<BookModel, UUID, BooksFilter>(BookModel::class, BookModelDraft.`$`.type) {

    override fun sortingExpressions(table: KNonNullTable<BookModel>): List<KPropExpression<out Comparable<*>>> =
        listOf(table.name, table.updatedAt, table.id)

    override fun toPredicates(context: CoroutineContext, filter: BooksFilter, table: KNonNullTable<BookModel>): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.id.accept(filter.id),
            table.name.accept(filter.name),
            table.authorIds.acceptMany(filter.authorIds),
            filter.authors?.let {
                table.authors {
                    registry.toPredicates(context, it, asTableEx())
                }
            }
        )
    }
}
