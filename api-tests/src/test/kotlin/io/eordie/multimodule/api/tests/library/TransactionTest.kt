package io.eordie.multimodule.api.tests.library

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.api.tests.AbstractApplicationTest
import io.eordie.multimodule.library.models.AuthorModel
import io.eordie.multimodule.library.models.AuthorModelDraft
import io.eordie.multimodule.library.models.BookModel
import io.eordie.multimodule.library.models.BookModelDraft
import io.eordie.multimodule.library.repository.AuthorsFactory
import io.eordie.multimodule.library.repository.BooksFactory
import io.micronaut.transaction.exceptions.UnexpectedRollbackException
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TransactionTest : AbstractApplicationTest() {
    @Inject
    lateinit var books: BooksFactory

    @Inject
    lateinit var authors: AuthorsFactory

    @Test
    fun `should rollback transaction`() = runBlocking(authorization) {
        var book = BookModel {}
        var author = AuthorModel {}

        val e = assertThrows<UnexpectedRollbackException> {
            books.transaction {
                author = authors.save<AuthorModelDraft> {
                    this.firstName = "Test1"
                    this.lastName = null
                }

                assertThat(authors.findById(author.id)).isNotNull()

                book = books.save<BookModelDraft> {
                    this.name = "Test1"
                    this.authorIds = listOf(author.id)
                }

                assertThat(books.findById(book.id)).isNotNull()
                error("rollback")
            }
        }
        assertThat(e).isNotNull()
        assertThat(e.message).contains("rollback")
        assertThat(book.id).isNotNull()
        assertThat(books.findById(book.id)).isNull()
        assertThat(author.id).isNotNull()
        assertThat(authors.findById(author.id)).isNull()
    }

    @Test
    fun `should rollback inner transactions`() = runBlocking(authorization) {
        var book = BookModel {}
        var author = AuthorModel {}

        val e = assertThrows<UnexpectedRollbackException> {
            books.transaction {
                author = authors.transaction {
                    authors.save<AuthorModelDraft> {
                        this.firstName = "Test2"
                        this.lastName = null
                    }
                }

                assertThat(authors.findById(author.id)).isNotNull()

                book = books.transaction {
                    books.save<BookModelDraft> {
                        this.name = "Test2"
                        this.authorIds = listOf(author.id)
                    }
                }

                assertThat(books.findById(book.id)).isNotNull()
                error("rollback")
            }
        }
        assertThat(e).isNotNull()
        assertThat(e.message).contains("rollback")
        assertThat(book.id).isNotNull()
        assertThat(books.findById(book.id)).isNull()
        assertThat(author.id).isNotNull()
        assertThat(authors.findById(author.id)).isNull()
    }

    @Test
    fun `should rollback on inner transaction exception`() = runBlocking(authorization) {
        var author = AuthorModel {}

        val e = assertThrows<UnexpectedRollbackException> {
            books.transaction {
                author = authors.transaction {
                    authors.save<AuthorModelDraft> {
                        this.firstName = "Test2"
                        this.lastName = null
                    }
                }

                assertThat(authors.findById(author.id)).isNotNull()

                books.transaction {
                    error("rollback")
                }
            }
        }
        assertThat(e).isNotNull()
        assertThat(e.message).contains("rollback")
        assertThat(author.id).isNotNull()
        assertThat(authors.findById(author.id)).isNull()
    }

    @Test
    fun `should rollback inner transactions by sub coroutines`() = runBlocking(authorization) {
        var book = BookModel {}
        var author = AuthorModel {}

        val e = assertThrows<UnexpectedRollbackException> {
            books.transaction {
                author = coroutineScope {
                    authors.transaction {
                        authors.save<AuthorModelDraft> {
                            this.firstName = "Test3"
                            this.lastName = null
                        }
                    }
                }

                assertThat(authors.findById(author.id)).isNotNull()

                book = coroutineScope {
                    books.transaction {
                        books.save<BookModelDraft> {
                            this.name = "Test3"
                            this.authorIds = listOf(author.id)
                        }
                    }
                }

                assertThat(books.findById(book.id)).isNotNull()
                error("rollback")
            }
        }
        assertThat(e).isNotNull()
        assertThat(e.message).contains("rollback")
        assertThat(book.id).isNotNull()
        assertThat(books.findById(book.id)).isNull()
        assertThat(author.id).isNotNull()
        assertThat(authors.findById(author.id)).isNull()
    }

    @Test
    fun `should rollback inner transactions by async coroutines`() = runBlocking(authorization) {
        var book = BookModel {}
        var author = AuthorModel {}

        val e = assertThrows<UnexpectedRollbackException> {
            books.transaction {
                author = async {
                    authors.transaction {
                        authors.save<AuthorModelDraft> {
                            this.firstName = "Test4"
                            this.lastName = null
                        }
                    }
                }.await()

                assertThat(authors.findById(author.id)).isNotNull()

                book = async {
                    books.transaction {
                        books.save<BookModelDraft> {
                            this.name = "Test4"
                            this.authorIds = listOf(author.id)
                        }
                    }
                }.await()

                assertThat(books.findById(book.id)).isNotNull()
                error("rollback")
            }
        }
        assertThat(e).isNotNull()
        assertThat(e.message).contains("rollback")
        assertThat(book.id).isNotNull()
        assertThat(books.findById(book.id)).isNull()
        assertThat(author.id).isNotNull()
        assertThat(authors.findById(author.id)).isNull()
    }
}
