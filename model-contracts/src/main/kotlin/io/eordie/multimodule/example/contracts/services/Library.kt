package io.eordie.multimodule.example.contracts.services

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import io.eordie.multimodule.example.contracts.Query
import io.eordie.multimodule.example.contracts.models.Author
import io.eordie.multimodule.example.contracts.models.Book
import java.util.*

interface Library : Query {
    suspend fun bookById(id: UUID): Book

    suspend fun authorById(id: UUID): Author

    @GraphQLIgnore
    suspend fun booksByAuthors(authorIds: List<UUID>, bookName: String?): Map<UUID, List<Book>>
}
