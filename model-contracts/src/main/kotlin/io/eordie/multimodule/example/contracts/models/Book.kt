package io.eordie.multimodule.example.contracts.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.getValueBy
import io.eordie.multimodule.example.contracts.services.Library
import io.micronaut.core.annotation.Introspected
import java.util.*
import java.util.concurrent.CompletableFuture

@Introspected
data class Book(
    val id: UUID,
    val name: String,
    val authorId: UUID
) {
    fun author(env: DataFetchingEnvironment): CompletableFuture<Author> {
        return env.getValueBy(Library::authorById, authorId)
    }
}
