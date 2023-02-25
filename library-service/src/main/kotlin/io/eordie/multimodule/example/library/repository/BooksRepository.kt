package io.eordie.multimodule.example.library.repository

import io.eordie.multimodule.example.contracts.library.models.BookSummary
import io.eordie.multimodule.example.contracts.library.models.BooksFilter
import io.eordie.multimodule.example.library.models.BookModel
import io.eordie.multimodule.example.repository.FilterSupportTrait
import io.eordie.multimodule.example.repository.KFactory
import io.eordie.multimodule.example.repository.KRepository
import io.micronaut.data.annotation.Query
import java.util.*

@KRepository
interface BooksRepository : KFactory<BookModel, UUID>, FilterSupportTrait<BookModel, UUID, BooksFilter> {

    @Query(
        """
        select
            array_agg(distinct tb_1_.id) as ids,
            count(distinct tb_1_.id) as totalCount, 
            array_agg(distinct author_id) as authorIds,
            array_agg(distinct tb_1_.name) as names
        from library_books tb_1_, unnest(tb_1_.author_ids) as author_id                                       
        """
    )
    suspend fun getBooksSummary(filter: BooksFilter): BookSummary
}
