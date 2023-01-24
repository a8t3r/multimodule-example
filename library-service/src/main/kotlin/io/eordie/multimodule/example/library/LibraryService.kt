package io.eordie.multimodule.example.library

import io.eordie.multimodule.example.contracts.models.Author
import io.eordie.multimodule.example.contracts.models.Book
import io.eordie.multimodule.example.contracts.services.Library
import jakarta.inject.Singleton
import java.util.*

@Singleton
class LibraryService : Library {
    override suspend fun bookById(id: UUID): Book {
        return Book(id, "foobar", UUID.randomUUID())
    }

    override suspend fun authorById(id: UUID): Author {
        return Author(id, "barfoo")
    }

    override suspend fun booksByAuthors(authorIds: List<UUID>, bookName: String?): Map<UUID, List<Book>> {
        val name = bookName ?: "empty"
        return authorIds.associateWith {
            listOf(
                Book(UUID.randomUUID(), name, it),
                Book(UUID.randomUUID(), name, it)
            )
        }
    }
}
