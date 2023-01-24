package io.eordie.multimodule.example.contracts.library.services

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import io.eordie.multimodule.example.contracts.library.models.Author
import io.eordie.multimodule.example.contracts.library.models.Book
import io.eordie.multimodule.example.contracts.utils.Query
import java.util.*

interface Library : Query {
    suspend fun bookById(id: UUID): Book

    suspend fun authorById(id: UUID): Author

    @GraphQLIgnore
    suspend fun booksByAuthors(authorIds: List<UUID>, bookName: String?): Map<UUID, List<Book>>
}
