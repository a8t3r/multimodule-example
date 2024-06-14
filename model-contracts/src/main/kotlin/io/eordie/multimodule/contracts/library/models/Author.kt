package io.eordie.multimodule.contracts.library.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.Auditable
import io.eordie.multimodule.contracts.annotations.Cached
import io.eordie.multimodule.contracts.library.services.Library
import io.eordie.multimodule.contracts.utils.OffsetDateTimeStr
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.getValueBy
import io.eordie.multimodule.contracts.utils.orDefault
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

@Cached
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

    fun fullName(): String = "$firstName ${lastName ?: "<unspecified>"}"

    fun books(env: DataFetchingEnvironment, filter: BooksFilter? = null): CompletableFuture<List<Book>> {
        return env.getValueBy(Library::loadBooksByAuthors, id, filter.orDefault())
    }
}
