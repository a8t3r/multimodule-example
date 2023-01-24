package io.eordie.multimodule.example.contracts.library.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.library.services.Library
import io.eordie.multimodule.example.contracts.utils.UUIDAsString
import io.eordie.multimodule.example.contracts.utils.getValueBy
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

@Serializable
@Introspected
data class Author(
    val id: UUIDAsString,
    val name: String
) {
    fun books(env: DataFetchingEnvironment, bookName: String? = null): CompletableFuture<List<Book>> {
        return env.getValueBy(Library::booksByAuthors, id, bookName)
    }
}
