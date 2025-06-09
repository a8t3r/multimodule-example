package io.eordie.multimodule.library.service

import io.eordie.multimodule.common.repository.event.EventListener
import io.eordie.multimodule.contracts.basic.event.MutationEvent
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BooksFilter
import io.eordie.multimodule.contracts.library.services.LibrarySubscriptions
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.Topic
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
@KafkaListener
class LibrarySubscriptionController : LibrarySubscriptions, EventListener<Book> {

    private val booksFlow = MutableSharedFlow<Book>(
        extraBufferCapacity = 3, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    @Topic("book")
    override suspend fun onEvent(
        causedBy: AuthenticationDetails?,
        event: MutationEvent<Book>
    ) {
        if (event.isCreated()) {
            booksFlow.emit(event.getActual())
        }
    }

    override suspend fun books(filter: BooksFilter?): Flow<Book> = booksFlow.asSharedFlow()
}
