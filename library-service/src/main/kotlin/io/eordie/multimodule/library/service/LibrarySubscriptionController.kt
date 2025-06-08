package io.eordie.multimodule.library.service

import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BooksFilter
import io.eordie.multimodule.contracts.library.services.LibrarySubscriptions
import io.eordie.multimodule.contracts.utils.orDefault
import io.eordie.multimodule.library.repository.BooksFactory
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LibrarySubscriptionController(
    private val books: BooksFactory
) : LibrarySubscriptions {

    override suspend fun books(filter: BooksFilter?): Flow<Book> {
        return books.findAllByFilter(filter.orDefault()).map { it.convert() }
    }
}
