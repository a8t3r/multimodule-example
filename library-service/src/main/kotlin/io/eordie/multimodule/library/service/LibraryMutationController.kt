package io.eordie.multimodule.library.service

import io.eordie.multimodule.contracts.library.models.Author
import io.eordie.multimodule.contracts.library.models.AuthorInput
import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BookInput
import io.eordie.multimodule.contracts.library.models.BookUpdate
import io.eordie.multimodule.contracts.library.services.LibraryMutations
import io.eordie.multimodule.library.models.AuthorModelDraft
import io.eordie.multimodule.library.models.BookModelDraft
import io.eordie.multimodule.library.repository.AuthorsFactory
import io.eordie.multimodule.library.repository.BooksFactory
import jakarta.inject.Singleton
import java.util.*

@Singleton
class LibraryMutationController(
    private val books: BooksFactory,
    private val authors: AuthorsFactory
) : LibraryMutations {

    override suspend fun book(input: BookInput): Book {
        val authorIds = input.authors.map { author ->
            authors.save<AuthorModelDraft>(author.id) { _, instance ->
                instance.firstName = requireNotNull(author.firstName ?: instance.firstName)
                instance.lastName = author.lastName ?: instance.lastName
            }
        }.map { it.id }

        return books.save<BookModelDraft> {
            this.name = input.name
            this.authorIds = authorIds.distinct()
        }.convert()
    }

    override suspend fun deleteBook(bookId: UUID): Boolean {
        return books.deleteById(bookId)
    }

    override suspend fun updateBook(update: BookUpdate): Book {
        return books.update<BookModelDraft>(update.id) {
            this.deleted = false
            this.name = update.name ?: this.name
            this.authorIds = update.authorIds ?: this.authorIds
        }.convert()
    }

    override suspend fun author(input: AuthorInput): Author {
        return authors.save<AuthorModelDraft> {
            this.firstName = requireNotNull(input.firstName)
            this.lastName = input.lastName
        }.convert()
    }
}
