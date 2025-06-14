package io.eordie.multimodule.library.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.filter.acceptMany
import io.eordie.multimodule.common.repository.KBaseFactory
import io.eordie.multimodule.common.repository.ext.arraySize
import io.eordie.multimodule.common.repository.ext.negateUnless
import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BooksFilter
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.library.models.BookModel
import io.eordie.multimodule.library.models.BookModelDraft
import io.eordie.multimodule.library.models.authorIds
import io.eordie.multimodule.library.models.authors
import io.eordie.multimodule.library.models.id
import io.eordie.multimodule.library.models.name
import io.eordie.multimodule.library.models.numberOfAuthors
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class BooksFactory : KBaseFactory<BookModel, BookModelDraft, Book, UUID, BooksFilter>(BookModel::class) {

    override fun sortingExpressions(table: KNonNullTable<BookModel>): List<KPropExpression<out Comparable<*>>> {
        return super.sortingExpressions(table) + table.numberOfAuthors
    }

    override fun ResourceAcl.toPredicates(filter: BooksFilter, table: KNonNullTable<BookModel>): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.id accept filter.id,
            table.name accept filter.name,
            table.authorIds acceptMany filter.authorIds,
            table.authorIds.arraySize accept filter.authorIdsSize,
            table.authors { accept(filter.authors) }.negateUnless(filter.hasAuthors)
        )
    }
}
