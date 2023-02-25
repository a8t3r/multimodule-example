package io.eordie.multimodule.example.contracts.library.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.Auditable
import io.eordie.multimodule.example.contracts.OutputOnly
import io.eordie.multimodule.example.contracts.utils.OffsetDateTimeStr
import io.eordie.multimodule.example.contracts.utils.UuidStr
import io.eordie.multimodule.example.contracts.utils.byIds
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

@OutputOnly
@Introspected
@Serializable
data class Book(
    val id: UuidStr,
    val name: String,
    val authorIds: List<UuidStr>,
    override val deleted: Boolean,
    override val createdAt: OffsetDateTimeStr,
    override val updatedAt: OffsetDateTimeStr
) : Auditable {
    fun authors(env: DataFetchingEnvironment): CompletableFuture<List<Author>> = env.byIds(authorIds)
}
