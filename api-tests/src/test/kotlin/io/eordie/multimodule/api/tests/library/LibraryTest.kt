package io.eordie.multimodule.api.tests.library

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.api.tests.AbstractApplicationTest
import io.eordie.multimodule.contracts.basic.exception.EntityNotFoundException
import io.eordie.multimodule.contracts.basic.filters.OffsetDateTimeLiteralFilter
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.basic.paging.SortOrder
import io.eordie.multimodule.contracts.library.models.AuthorInput
import io.eordie.multimodule.contracts.library.models.AuthorsFilter
import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BookInput
import io.eordie.multimodule.contracts.library.models.BookUpdate
import io.eordie.multimodule.contracts.library.models.BooksFilter
import io.eordie.multimodule.contracts.library.services.Library
import io.eordie.multimodule.contracts.library.services.LibraryMutations
import io.micronaut.test.annotation.Sql
import jakarta.inject.Inject
import kotlinx.coroutines.future.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime

@Sql("truncate-library.sql", phase = Sql.Phase.BEFORE_ALL)
class LibraryTest : AbstractApplicationTest() {

    @Inject
    lateinit var queryLibrary: Library

    @Inject
    lateinit var mutateLibrary: LibraryMutations

    private lateinit var firstBook: Book
    private lateinit var secondBook: Book

    @BeforeAll
    fun init() = test {
        firstBook = mutateLibrary.book(
            BookInput("First book", listOf(AuthorInput(firstName = "John", lastName = "Doe")))
        )
        secondBook = mutateLibrary.book(
            BookInput("Second book", listOf(AuthorInput(firstName = "Jane", lastName = "Doe")))
        )
    }

    @AfterAll
    fun destroy() = test {
        if (::firstBook.isInitialized) { mutateLibrary.deleteBook(firstBook.id) }
        if (::secondBook.isInitialized) { mutateLibrary.deleteBook(secondBook.id) }
    }

    private fun compareBooks(actual: Book, expected: Book) {
        assertThat(actual.id).isEqualTo(expected.id)
        assertThat(actual.name).isEqualTo(expected.name)
        assertThat(actual.authorIds).isEqualTo(expected.authorIds)
    }

    @Test
    fun `should retrieve authors from book`() = test {
        val authors = firstBook.authors(env()).await()
        assertThat(authors).hasSize(1)

        val author = authors[0]
        assertThat(author.firstName).isEqualTo("John")
        assertThat(author.lastName).isEqualTo("Doe")
    }

    @Test
    fun `should query books`() = test {
        val books = queryLibrary.books(BooksFilter(), Pageable(orderBy = listOf(SortOrder("name"))))
        assertThat(books).isNotNull()
        assertThat(books.pageable.cursor).isNull()
        assertThat(books.data).hasSize(2)
        compareBooks(books.data[0], firstBook)
        compareBooks(books.data[1], secondBook)
    }

    @Test
    fun `should query book by id filter`() = test {
        val books = queryLibrary.books(BooksFilter(id = UUIDLiteralFilter(eq = firstBook.id)))
        assertThat(books).isNotNull()
        assertThat(books.pageable.cursor).isNull()
        assertThat(books.data).hasSize(1)
        compareBooks(books.data[0], firstBook)
    }

    @Test
    fun `should return empty page on false condition`() = test {
        val books = queryLibrary.books(
            BooksFilter(createdAt = OffsetDateTimeLiteralFilter(lt = OffsetDateTime.now().minusDays(1)))
        )
        assertThat(books).isNotNull()
        assertThat(books.pageable.cursor).isNull()
        assertThat(books.data).isEmpty()
    }

    @Test
    fun `should query book by name`() = test {
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
    fun `should query books by complex filter`() = test {
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
    fun `should iterate books`() = test {
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
    fun `should delete book and recreate it`() = test {
        assertThat(mutateLibrary.deleteBook(firstBook.id)).isTrue()

        val actual = queryLibrary.bookById(firstBook.id)
        assertThat(actual).isNull()

        val e = assertThrows<EntityNotFoundException> {
            mutateLibrary.updateBook(BookUpdate(firstBook.id, name = firstBook.name))
        }
        assertThat(e.entityId).isEqualTo(firstBook.id.toString())
        assertThat(e.entityType).isEqualTo("BookModel")

        val books = queryLibrary.books(BooksFilter(id = UUIDLiteralFilter(eq = firstBook.id)))
        assertThat(books.pageable.cursor).isNull()
        assertThat(books.data).isEmpty()

        val bookInput = BookInput("First book", listOf(AuthorInput(firstName = "John", lastName = "Doe")))
        firstBook = mutateLibrary.book(bookInput)
    }
}
