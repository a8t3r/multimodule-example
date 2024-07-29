package io.eordie.multimodule.library.service

import io.eordie.multimodule.common.utils.asPageFetcher
import io.eordie.multimodule.common.utils.associateByIds
import io.eordie.multimodule.common.utils.convert
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.basic.paging.SelectionSet
import io.eordie.multimodule.contracts.library.models.Author
import io.eordie.multimodule.contracts.library.models.AuthorsFilter
import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BookSummary
import io.eordie.multimodule.contracts.library.models.BooksFilter
import io.eordie.multimodule.contracts.library.services.Library
import io.eordie.multimodule.contracts.utils.orDefault
import io.eordie.multimodule.library.models.BookModel
import io.eordie.multimodule.library.repository.AuthorsFactory
import io.eordie.multimodule.library.repository.BooksFactory
import io.eordie.multimodule.library.repository.BooksRepository
import jakarta.inject.Singleton
import java.util.*

@Singleton
class LibraryController(
    private val books: BooksFactory,
    private val authors: AuthorsFactory,
    private val booksRepository: BooksRepository
) : Library {

    override suspend fun bookById(id: UUID): Book? {
        return booksRepository.findById(id)?.convert()
    }

    override suspend fun authorById(id: UUID): Author? {
        return authors.findById(id)?.convert()
    }

    override suspend fun books(
        selectionSet: SelectionSet,
        filter: BooksFilter?,
        pageable: Pageable?
    ): Page<Book> {
        return books.findByFilter(filter.orDefault(), pageable, selectionSet.asPageFetcher()).convert()
    }

    override suspend fun bookSummary(filter: BooksFilter?): BookSummary {
        return booksRepository.getBooksSummary(filter.orDefault())
    }

    override suspend fun authors(filter: AuthorsFilter?, pageable: Pageable?): Page<Author> {
        return authors.findByFilter(filter.orDefault(), pageable).convert()
    }

    override suspend fun loadBooksByAuthors(authorIds: List<UUID>, filter: BooksFilter?): Map<UUID, List<Book>> {
        val filterBy = filter.orDefault().copy(authorIds = UUIDLiteralFilter(of = authorIds))
        return books.findAllByFilter(filterBy).associateByIds(authorIds, BookModel::authorIds) { it.convert() }
    }
}
