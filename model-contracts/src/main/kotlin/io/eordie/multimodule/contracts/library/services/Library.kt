package io.eordie.multimodule.contracts.library.services

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.basic.paging.SelectionSet
import io.eordie.multimodule.contracts.library.models.Author
import io.eordie.multimodule.contracts.library.models.AuthorsFilter
import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BookSummary
import io.eordie.multimodule.contracts.library.models.BooksFilter
import java.util.*

@AutoService(Query::class)
interface Library : Query {
    suspend fun bookById(id: UUID): Book?

    suspend fun books(
        @GraphQLIgnore selectionSet: SelectionSet,
        filter: BooksFilter?,
        pageable: Pageable? = null
    ): Page<Book>

    suspend fun bookSummary(filter: BooksFilter?): BookSummary

    suspend fun authorById(id: UUID): Author?

    suspend fun authors(filter: AuthorsFilter?, pageable: Pageable? = null): Page<Author>

    suspend fun loadBooksByAuthors(authorIds: List<UUID>, filter: BooksFilter? = null): Map<UUID, List<Book>>
}
