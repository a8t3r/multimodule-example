package io.eordie.multimodule.contracts.basic

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.basic.paging.Pageable

@AutoService(Query::class)
interface SchemaServiceHints : Query {
    fun pageable(pageable: Pageable): Pageable
}
