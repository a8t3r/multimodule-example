package io.eordie.multimodule.contracts.library.services

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.library.models.Author
import io.eordie.multimodule.contracts.library.models.AuthorInput
import io.eordie.multimodule.contracts.library.models.Book
import io.eordie.multimodule.contracts.library.models.BookInput
import io.eordie.multimodule.contracts.library.models.BookUpdate
import java.util.*

@AutoService(Mutation::class)
interface LibraryMutations : Mutation {
    suspend fun book(input: BookInput): Book

    suspend fun deleteBook(bookId: UUID): Boolean

    suspend fun updateBook(update: BookUpdate): Book

    suspend fun author(input: AuthorInput): Author
}
