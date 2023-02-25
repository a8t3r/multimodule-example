package io.eordie.multimodule.example.apitests.library

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.example.apitests.AbstractApplicationTest
import io.eordie.multimodule.example.apitests.AuthUtils.authWith
import io.eordie.multimodule.example.contracts.basic.exception.EntityNotFoundException
import io.eordie.multimodule.example.contracts.basic.filters.OffsetDateTimeLiteralFilter
import io.eordie.multimodule.example.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.example.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.example.contracts.basic.paging.Pageable
import io.eordie.multimodule.example.contracts.basic.paging.SortOrder
import io.eordie.multimodule.example.contracts.library.models.AuthorInput
import io.eordie.multimodule.example.contracts.library.models.AuthorsFilter
import io.eordie.multimodule.example.contracts.library.models.Book
import io.eordie.multimodule.example.contracts.library.models.BookInput
import io.eordie.multimodule.example.contracts.library.models.BookUpdate
import io.eordie.multimodule.example.contracts.library.models.BooksFilter
import io.eordie.multimodule.example.contracts.library.services.Library
import io.eordie.multimodule.example.contracts.library.services.LibraryMutations
import jakarta.inject.Inject
import kotlinx.coroutines.future.await
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.reflect.InvocationTargetException
import java.time.OffsetDateTime

class LibraryTest : AbstractApplicationTest() {

    companion object {
        val libraryManager = authWith(defaultOrganization)
    }

    @Inject
    lateinit var queryLibrary: Library

    @Inject
    lateinit var mutateLibrary: LibraryMutations

    private lateinit var firstBook: Book
    private lateinit var secondBook: Book

    @BeforeAll
    fun init() = runTest(libraryManager) {
        mutateLibrary.internalTruncate(
            BooksFilter(createdAt = OffsetDateTimeLiteralFilter(lt = OffsetDateTime.now())),
            AuthorsFilter(createdAt = OffsetDateTimeLiteralFilter(lt = OffsetDateTime.now()))
        )

        firstBook = mutateLibrary.book(
            BookInput("First book", listOf(AuthorInput(firstName = "John", lastName = "Doe")))
        )
        secondBook = mutateLibrary.book(
            BookInput("Second book", listOf(AuthorInput(firstName = "Jane", lastName = "Doe")))
        )
    }

    @AfterAll
    fun destroy() = runTest(libraryManager) {
        mutateLibrary.deleteBook(firstBook.id)
        mutateLibrary.deleteBook(secondBook.id)
    }

    private fun compareBooks(actual: Book, expected: Book) {
        assertThat(actual.id).isEqualTo(expected.id)
        assertThat(actual.name).isEqualTo(expected.name)
        assertThat(actual.authorIds).isEqualTo(expected.authorIds)
    }

    @Test
    fun `should retrieve authors from book`() = runTest(libraryManager) {
        val authors = firstBook.authors(env()).await()
        assertThat(authors).hasSize(1)

        val author = authors[0]
        assertThat(author.firstName).isEqualTo("John")
        assertThat(author.lastName).isEqualTo("Doe")
    }

    @Test
    fun `should query books`() = runTest(libraryManager) {
        val books = queryLibrary.books(BooksFilter(), Pageable(orderBy = listOf(SortOrder("name"))))
        assertThat(books).isNotNull()
        assertThat(books.pageable.cursor).isNull()
        assertThat(books.data).hasSize(2)
        compareBooks(books.data[0], firstBook)
        compareBooks(books.data[1], secondBook)
    }

    @Test
    fun `should query book by id filter`() = runTest(libraryManager) {
        val books = queryLibrary.books(BooksFilter(id = UUIDLiteralFilter(eq = firstBook.id)))
        assertThat(books).isNotNull()
        assertThat(books.pageable.cursor).isNull()
        assertThat(books.data).hasSize(1)
        compareBooks(books.data[0], firstBook)
    }

    @Test
    fun `should query book by name`() = runTest(libraryManager) {
        val books = queryLibrary.books(
            BooksFilter(
                name = StringLiteralFilter(
                    eq = firstBook.name,
                    startsWith = firstBook.name,
                    endsWith = firstBook.name,
                    like = firstBook.name
                )
            )
        )
        assertThat(books).isNotNull()
        assertThat(books.pageable.cursor).isNull()
        assertThat(books.data).hasSize(1)
        compareBooks(books.data[0], firstBook)
    }

    @Test
    fun `should query books by complex filter`() = runTest(libraryManager) {
        val books = queryLibrary.books(
            BooksFilter(
                name = StringLiteralFilter(
                    eq = firstBook.name,
                    startsWith = firstBook.name,
                    endsWith = firstBook.name,
                    like = firstBook.name
                ),
                authors = AuthorsFilter(
                    id = UUIDLiteralFilter(
                        of = firstBook.authorIds + secondBook.authorIds
                    ),
                    firstName = StringLiteralFilter(
                        of = listOf("John", "Foo", "Bar"),
                        exists = true,
                        startsWith = "Jo",
                        endsWith = "hn"
                    ),
                )
            )
        )
        assertThat(books).isNotNull()
        assertThat(books.pageable.cursor).isNull()
        assertThat(books.data).hasSize(1)
        compareBooks(books.data[0], firstBook)
    }

    @Test
    fun `should iterate books`() = runTest(libraryManager) {
        var books = queryLibrary.books(BooksFilter(), Pageable(limit = 1))
        assertThat(books.pageable.cursor).isNotNull()
        assertThat(books.data).hasSize(1)

        books = queryLibrary.books(BooksFilter(), Pageable(cursor = books.pageable.cursor, limit = 1))
        assertThat(books.pageable.cursor).isNotNull()
        assertThat(books.data).hasSize(1)

        books = queryLibrary.books(BooksFilter(), Pageable(cursor = books.pageable.cursor, limit = 1))
        assertThat(books.pageable.cursor).isNull()
        assertThat(books.data).isEmpty()
    }

    @Test
    fun `should delete book`() = runTest(libraryManager) {
        assertThat(mutateLibrary.deleteBook(firstBook.id)).isTrue()

        val actual = queryLibrary.bookById(firstBook.id)
        assertThat(actual).isNull()

        val e = assertThrows<InvocationTargetException> {
            mutateLibrary.updateBook(BookUpdate(firstBook.id, name = firstBook.name))
        }
        assertThat(e.targetException).isInstanceOf(EntityNotFoundException::class.java)

        val notFoundException = e.targetException as EntityNotFoundException
        assertThat(notFoundException.entityId).isEqualTo(firstBook.id)
        assertThat(notFoundException.entityType).isEqualTo("BookModel")

        val books = queryLibrary.books(BooksFilter(id = UUIDLiteralFilter(eq = firstBook.id)))
        assertThat(books.pageable.cursor).isNull()
        assertThat(books.data).isEmpty()
    }
}
