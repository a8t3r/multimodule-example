package io.eordie.multimodule.library.service

import io.eordie.multimodule.contracts.library.models.Author
import io.eordie.multimodule.contracts.library.models.AuthorInput
import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BookInput
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

    override suspend fun book(input: BookInput): Book = books.transaction {
        val authorIds = input.authors.map { author ->
            authors.save<AuthorModelDraft>(author.id) { _, instance ->
                instance.firstName = requireNotNull(author.firstName ?: instance.firstName)
                instance.lastName = author.lastName ?: instance.lastName
            }
        }.map { it.id }

        books.save<BookModelDraft>(input.id) { state, instance ->
            state.ifNotExists {
                instance.authorIds = emptyList()
            }

            instance.name = input.name
            if (authorIds.isNotEmpty()) {
                instance.authorIds = authorIds.distinct()
            }
        }
    }.convert()

    override suspend fun deleteBook(bookId: UUID): Boolean {
        return books.deleteById(bookId)
    }

    override suspend fun author(input: AuthorInput): Author {
        return authors.save<AuthorModelDraft>(input.id) { _, instance ->
            instance.firstName = requireNotNull(input.firstName ?: instance.firstName)
            instance.lastName = input.lastName
        }.convert()
    }
}
