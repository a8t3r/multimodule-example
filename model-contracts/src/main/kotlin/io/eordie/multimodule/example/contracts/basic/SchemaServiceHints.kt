package io.eordie.multimodule.example.contracts.basic

import com.google.auto.service.AutoService
import io.eordie.multimodule.example.contracts.Query
import io.eordie.multimodule.example.contracts.basic.paging.Pageable

@AutoService(Query::class)
interface SchemaServiceHints : Query {
    fun pageable(pageable: Pageable): Pageable
}
