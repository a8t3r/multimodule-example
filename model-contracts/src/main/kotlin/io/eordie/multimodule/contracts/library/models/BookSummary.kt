package io.eordie.multimodule.contracts.library.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.byIds
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

@Introspected
@Serializable
data class BookSummary(
    val totalCount: Long,
    val ids: List<UuidStr>,
    val names: List<String>,
    val authorIds: List<UuidStr>
) {
    fun books(env: DataFetchingEnvironment): CompletableFuture<List<Book>> = env.byIds(ids)

    fun authors(env: DataFetchingEnvironment): CompletableFuture<List<Author>> = env.byIds(authorIds)
}
