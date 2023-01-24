package io.eordie.multimodule.example.contracts.library.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.library.services.Library
import io.eordie.multimodule.example.contracts.utils.UUIDAsString
import io.eordie.multimodule.example.contracts.utils.getValueBy
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

@Introspected
@Serializable
data class Book(
    val id: UUIDAsString,
    val name: String,
    val authorId: UUIDAsString
) {
    fun author(env: DataFetchingEnvironment): CompletableFuture<Author> {
        return env.getValueBy(Library::authorById, authorId)
    }
}
