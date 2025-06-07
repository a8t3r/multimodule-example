package io.eordie.multimodule.api.tests.library

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.eordie.multimodule.api.tests.AbstractApplicationTest
import io.eordie.multimodule.common.repository.KFactory
import io.eordie.multimodule.library.models.AuthorModel
import io.eordie.multimodule.library.models.BookModel
import io.eordie.multimodule.library.repository.AuthorsFactory
import io.eordie.multimodule.library.repository.BooksFactory
import io.micronaut.transaction.exceptions.UnexpectedRollbackException
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class TransactionTest : AbstractApplicationTest() {
    @Inject
    lateinit var books: BooksFactory

    @Inject
    lateinit var authors: AuthorsFactory

    private suspend fun <T> KFactory<*, T, UUID>.ensureIsPresent(id: UUID) {
        val actual = findBySpecification { where(table.getId<UUID>() eq id) }.data
        assertThat(actual).hasSize(1)
        assertThat(existsById(id)).isTrue()
    }

    @Test
    fun `should rollback transaction`() = runBlocking(authorization) {
        var book = BookModel {}
        var author = AuthorModel {}

        val e = assertThrows<UnexpectedRollbackException> {
            books.transaction {
                author = authors.save {
                    this.firstName = "Test1"
                    this.lastName = null
                }

                authors.ensureIsPresent(author.id)

                book = books.save {
                    this.name = "Test1"
                    this.authorIds = listOf(author.id)
                }

                books.ensureIsPresent(book.id)
                error("rollback")
            }
        }
        assertThat(e).isNotNull()
        assertThat(e.message).isNotNull().contains("rollback")
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
                    authors.save {
                        this.firstName = "Test2"
                        this.lastName = null
                    }
                }

                authors.ensureIsPresent(author.id)

                book = books.transaction {
                    books.save {
                        this.name = "Test2"
                        this.authorIds = listOf(author.id)
                    }
                }

                books.ensureIsPresent(book.id)
                error("rollback")
            }
        }
        assertThat(e).isNotNull()
        assertThat(e.message).isNotNull().contains("rollback")
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
                    authors.save {
                        this.firstName = "Test2"
                        this.lastName = null
                    }
                }

                authors.ensureIsPresent(author.id)

                books.transaction {
                    error("rollback")
                }
            }
        }
        assertThat(e).isNotNull()
        assertThat(e.message).isNotNull().contains("rollback")
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
                        authors.save {
                            this.firstName = "Test3"
                            this.lastName = null
                        }
                    }
                }

                authors.ensureIsPresent(author.id)

                book = coroutineScope {
                    books.transaction {
                        books.save {
                            this.name = "Test3"
                            this.authorIds = listOf(author.id)
                        }
                    }
                }

                books.ensureIsPresent(book.id)
                error("rollback")
            }
        }
        assertThat(e).isNotNull()
        assertThat(e.message).isNotNull().contains("rollback")
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
                        authors.save {
                            this.firstName = "Test4"
                            this.lastName = null
                        }
                    }
                }.await()

                authors.ensureIsPresent(author.id)

                book = async {
                    books.transaction {
                        books.save {
                            this.name = "Test4"
                            this.authorIds = listOf(author.id)
                        }
                    }
                }.await()

                books.ensureIsPresent(book.id)
                error("rollback")
            }
        }
        assertThat(e).isNotNull()
        assertThat(e.message).isNotNull().contains("rollback")
        assertThat(book.id).isNotNull()
        assertThat(books.findById(book.id)).isNull()
        assertThat(author.id).isNotNull()
        assertThat(authors.findById(author.id)).isNull()
    }
}
