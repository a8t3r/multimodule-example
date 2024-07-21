package io.eordie.multimodule.library.repository

import io.eordie.multimodule.common.repository.KFactory
import io.eordie.multimodule.common.repository.KRepository
import io.eordie.multimodule.contracts.library.models.BookSummary
import io.eordie.multimodule.contracts.library.models.BooksFilter
import io.eordie.multimodule.library.models.BookModel
import io.micronaut.data.annotation.Query
import java.util.*

@KRepository
interface BooksRepository : KFactory<BookModel, UUID> {

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
