package io.eordie.multimodule.example.contracts.library.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.Auditable
import io.eordie.multimodule.example.contracts.OutputOnly
import io.eordie.multimodule.example.contracts.library.services.Library
import io.eordie.multimodule.example.contracts.utils.OffsetDateTimeStr
import io.eordie.multimodule.example.contracts.utils.UuidStr
import io.eordie.multimodule.example.contracts.utils.getValueBy
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

@OutputOnly
@Serializable
@Introspected
data class Author(
    val id: UuidStr,
    val firstName: String,
    val lastName: String?,
    override val deleted: Boolean,
    override val createdAt: OffsetDateTimeStr,
    override val updatedAt: OffsetDateTimeStr
) : Auditable {

    fun fullName(): String = "$firstName $lastName"

    fun books(env: DataFetchingEnvironment, filter: BooksFilter? = null): CompletableFuture<List<Book>> {
        val filterBy = filter ?: BooksFilter()
        return env.getValueBy(Library::loadBooksByAuthors, id, filterBy)
    }
}
