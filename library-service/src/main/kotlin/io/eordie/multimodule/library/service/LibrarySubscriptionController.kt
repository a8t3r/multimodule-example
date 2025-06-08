package io.eordie.multimodule.library.service

import io.eordie.multimodule.common.utils.RedisEventListener
import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BooksFilter
import io.eordie.multimodule.contracts.library.services.LibrarySubscriptions
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import jakarta.annotation.PostConstruct
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class LibrarySubscriptionController(
    private val connection: StatefulRedisPubSubConnection<String, Book>
) : LibrarySubscriptions {

    private val booksFlow = MutableSharedFlow<Book>(
        extraBufferCapacity = 3, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    @PostConstruct
    fun init() {
        connection.sync().subscribe("books")
        connection.addListener(RedisEventListener<Book> {
            booksFlow.tryEmit(it)
        })
    }

    override suspend fun books(filter: BooksFilter?): Flow<Book> = booksFlow.asSharedFlow()
}
