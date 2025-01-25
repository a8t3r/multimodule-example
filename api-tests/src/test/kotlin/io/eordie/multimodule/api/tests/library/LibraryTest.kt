package io.eordie.multimodule.api.tests.library

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.api.tests.AbstractApplicationTest
import io.eordie.multimodule.contracts.basic.filters.OffsetDateTimeLiteralFilter
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.basic.filters.UUIDLiteralFilter
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.basic.paging.SortDirection
import io.eordie.multimodule.contracts.basic.paging.SortOrder
import io.eordie.multimodule.contracts.library.models.AuthorInput
import io.eordie.multimodule.contracts.library.models.AuthorsFilter
import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BookInput
import io.eordie.multimodule.contracts.library.models.BooksFilter
import io.eordie.multimodule.contracts.library.services.Library
import io.eordie.multimodule.contracts.library.services.LibraryMutations
import io.micronaut.test.annotation.Sql
import jakarta.inject.Inject
import kotlinx.coroutines.future.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
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
            BookInput(null, "First book", listOf(AuthorInput(firstName = "John", lastName = "Doe")))
        )
        secondBook = mutateLibrary.book(
            BookInput(null, "Second book", listOf(AuthorInput(firstName = "Jane", lastName = "Doe")))
        )
    }

    @AfterAll
    fun destroy() = test {
        if (::firstBook.isInitialized) {
            mutateLibrary.deleteBook(firstBook.id)
        }
        if (::secondBook.isInitialized) {
            mutateLibrary.deleteBook(secondBook.id)
        }
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
    fun `should query books with formula sorting`() = test {
        val pageable = Pageable(
            orderBy = listOf(
                SortOrder("numberOfAuthors", SortDirection.DESC),
                SortOrder("name", SortDirection.DESC)
            )
        )

        val books = queryLibrary.books(BooksFilter(), pageable)
        assertThat(books).isNotNull()
        assertThat(books.pageable.cursor).isNull()
        assertThat(books.pageable.supportedOrders).containsAtLeast("name", "numberOfAuthors")
        assertThat(books.data).hasSize(2)
        compareBooks(books.data[0], secondBook)
        compareBooks(books.data[1], firstBook)
    }

    @Test
    fun `should get summary of books`() = test {
        val summary = queryLibrary.bookSummary(BooksFilter())
        assertThat(summary.totalCount).isEqualTo(2)
        assertThat(summary.ids).containsExactly(firstBook.id, secondBook.id)
        assertThat(summary.names).containsExactly(firstBook.name, secondBook.name)
        assertThat(summary.authorIds).isNotEmpty()
    }

    @Test
    fun `should get summary of single book`() = test {
        val summary = queryLibrary.bookSummary(BooksFilter(id = UUIDLiteralFilter(eq = firstBook.id)))
        assertThat(summary.totalCount).isEqualTo(1)
        assertThat(summary.ids).containsExactly(firstBook.id)
        assertThat(summary.names).containsExactly(firstBook.name)
        assertThat(summary.authorIds).isEqualTo(firstBook.authorIds)
    }

    @Test
    fun `should get summary by complex filter`() = test {
        val initial = BooksFilter(
            authors = AuthorsFilter(
                firstName = StringLiteralFilter(like = "John"),
                lastName = StringLiteralFilter(eq = "Doe")
            )
        )
        val filter = initial.copy(authors = initial.authors?.copy(books = initial))

        val summary = queryLibrary.bookSummary(filter)
        assertThat(summary.totalCount).isEqualTo(1)
        assertThat(summary.ids).containsExactly(firstBook.id)
        assertThat(summary.names).containsExactly(firstBook.name)
        assertThat(summary.authorIds).isEqualTo(firstBook.authorIds)
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
                        nil = false,
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
        val bookId = firstBook.id
        assertThat(mutateLibrary.deleteBook(bookId)).isTrue()

        val actual = queryLibrary.bookById(bookId)
        assertThat(actual).isNull()

        assertThat(queryLibrary.bookById(bookId)).isNull()

        val books = queryLibrary.books(BooksFilter(id = UUIDLiteralFilter(eq = bookId)))
        assertThat(books.pageable.cursor).isNull()
        assertThat(books.data).isEmpty()

        val bookInput = BookInput(bookId, "Third book", listOf(AuthorInput(firstName = "Jane", lastName = "Doe")))
        firstBook = mutateLibrary.book(bookInput)
        assertThat(firstBook.id).isNotEqualTo(bookInput.id)
        assertThat(firstBook.name).isEqualTo("Third book")
        assertThat(firstBook.authorIds).hasSize(1)
    }

    @Test
    fun `should retrieve authors from book by graphql query`() = test {
        val result = context.query {
            books {
                data {
                    id()
                    name()
                    authors {
                        id()
                        fullName()
                    }
                }
            }
        }

        assertThat(result.books.data).hasSize(2)
        result.books.data?.forEach { book ->
            assertThat(book?.authors).hasSize(1)
        }
    }
}
