package io.eordie.multimodule.common.repository

import io.eordie.multimodule.common.rsocket.context.getSelectionSet
import io.eordie.multimodule.common.utils.FetcherBuilder
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import kotlinx.coroutines.flow.Flow
import org.babyfish.jimmer.sql.fetcher.Fetcher
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass

interface ProjectionSupportTrait<T : Convertable<*>, ID, F : Any> : FilterSupportTrait<T, ID, F> {

    val entityType: KClass<T>

    suspend fun query(filter: F, pageable: Pageable?): Page<T> {
        val fetcher = fetcherBySelectionSet()
        return findByFilter(filter, pageable, fetcher)
    }

    suspend fun queryAll(filter: F): Flow<T> {
        val fetcher = fetcherBySelectionSet()
        return findAllByFilter(filter, fetcher)
    }

    private suspend fun fetcherBySelectionSet(): Fetcher<T>? {
        val selectionSet = coroutineContext.getSelectionSet()
        val fetcher = if (selectionSet == null) null else {
            val fields = selectionSet.fields.map { it.substringAfter("${Page<T>::data.name}.") }
            FetcherBuilder(entityType).newFetcher(fields)
        }
        return fetcher
    }
}
