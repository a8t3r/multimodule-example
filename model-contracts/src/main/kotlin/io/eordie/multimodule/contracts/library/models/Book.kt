package io.eordie.multimodule.contracts.library.models

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.Auditable
import io.eordie.multimodule.contracts.utils.OffsetDateTimeStr
import io.eordie.multimodule.contracts.utils.UuidStr
import io.eordie.multimodule.contracts.utils.byIds
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.concurrent.CompletableFuture

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
