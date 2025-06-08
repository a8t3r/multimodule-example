package io.eordie.multimodule.contracts.library.services

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Subscription
import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BooksFilter
import kotlinx.coroutines.flow.Flow

@AutoService(Subscription::class)
interface LibrarySubscriptions : Subscription {
    suspend fun books(filter: BooksFilter? = null): Flow<Book>
}
