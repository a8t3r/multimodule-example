package io.eordie.multimodule.example.contracts.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.getValueBy
import io.eordie.multimodule.example.contracts.services.Library
import io.micronaut.core.annotation.Introspected
import java.util.*
import java.util.concurrent.CompletableFuture

@Introspected
data class Author(
    val id: UUID,
    val name: String
) {
    fun books(env: DataFetchingEnvironment, bookName: String? = null): CompletableFuture<List<Book>> {
        return env.getValueBy(Library::booksByAuthors, id, bookName)
    }
}
