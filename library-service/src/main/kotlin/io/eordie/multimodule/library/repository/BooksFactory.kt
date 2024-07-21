package io.eordie.multimodule.library.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.filter.acceptMany
import io.eordie.multimodule.common.repository.KBaseFactory
import io.eordie.multimodule.common.repository.ResourceAcl
import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BooksFilter
import io.eordie.multimodule.library.models.BookModel
import io.eordie.multimodule.library.models.authorIds
import io.eordie.multimodule.library.models.authors
import io.eordie.multimodule.library.models.id
import io.eordie.multimodule.library.models.name
import io.eordie.multimodule.library.models.updatedAt
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class BooksFactory : KBaseFactory<BookModel, Book, UUID, BooksFilter>(BookModel::class) {

    override fun sortingExpressions(table: KNonNullTable<BookModel>): List<KPropExpression<out Comparable<*>>> =
        listOf(table.name, table.updatedAt, table.id)

    override fun toPredicates(acl: ResourceAcl, filter: BooksFilter, table: KNonNullTable<BookModel>): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.id.accept(filter.id),
            table.name.accept(filter.name),
            table.authorIds.acceptMany(filter.authorIds),
            table.asTableEx().authors.accept(acl, filter.authors)
        )
    }
}
