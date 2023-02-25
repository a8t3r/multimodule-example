package io.eordie.multimodule.example.library.service

import io.eordie.multimodule.example.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.example.contracts.basic.paging.Page
import io.eordie.multimodule.example.contracts.basic.paging.Pageable
import io.eordie.multimodule.example.contracts.library.models.Author
import io.eordie.multimodule.example.contracts.library.models.AuthorsFilter
import io.eordie.multimodule.example.contracts.library.models.Book
import io.eordie.multimodule.example.contracts.library.models.BookSummary
import io.eordie.multimodule.example.contracts.library.models.BooksFilter
import io.eordie.multimodule.example.contracts.library.services.Library
import io.eordie.multimodule.example.library.models.BookModel
import io.eordie.multimodule.example.library.repository.AuthorsFactory
import io.eordie.multimodule.example.library.repository.BooksRepository
import io.eordie.multimodule.example.utils.associateByList
import io.eordie.multimodule.example.utils.convert
import jakarta.inject.Singleton
import java.util.*

@Singleton
class LibraryController(
    private val books: BooksRepository,
    private val authors: AuthorsFactory
) : Library {

    override suspend fun bookById(id: UUID): Book? {
        return books.findById(id)?.convert()
    }

    override suspend fun authorById(id: UUID): Author? {
        return authors.findById(id)?.convert()
    }

    override suspend fun books(filter: BooksFilter?, pageable: Pageable?): Page<Book> {
        return books.findByFilter(filter ?: BooksFilter(), pageable = pageable ?: Pageable()).convert()
    }

    override suspend fun bookSummary(filter: BooksFilter?): BookSummary {
        return books.getBooksSummary(filter ?: BooksFilter())
    }

    override suspend fun authors(filter: AuthorsFilter?, pageable: Pageable?): Page<Author> {
        return authors.findByFilter(filter ?: AuthorsFilter(), pageable = pageable ?: Pageable()).convert()
    }

    override suspend fun loadBooksByAuthors(authorIds: List<UUID>, filter: BooksFilter?): Map<UUID, List<Book>> {
        val filterBy = (filter ?: BooksFilter()).copy(authorIds = UUIDLiteralFilter(of = authorIds))
        return books.findByFilter(filterBy, pageable = Pageable()).associateByList(authorIds, BookModel::authorIds)
    }
}
