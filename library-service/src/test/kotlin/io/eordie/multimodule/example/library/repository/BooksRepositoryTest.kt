package io.eordie.multimodule.example.library.repository

import com.google.common.truth.Truth.assertThat
import io.eordie.multimodule.example.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.example.contracts.library.models.BooksFilter
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class BooksRepositoryTest : AbstractApplicationTest() {

    @Inject
    lateinit var repository: BooksRepository

    @Inject
    lateinit var books: BooksFactory

    @Test
    fun `foo test`() = runBlocking {
        val summary = repository.getBooksSummary(BooksFilter())
        assertThat(summary).isNotNull()
    }

    @Test
    fun `should retrieve models`() = runBlocking {
        val page = books.findByFilter(BooksFilter().copy(name = StringLiteralFilter(like = "foo")))
        assertThat(page).isNotNull()
    }
}
