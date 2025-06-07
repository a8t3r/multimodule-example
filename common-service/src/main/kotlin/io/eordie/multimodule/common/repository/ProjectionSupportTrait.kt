package io.eordie.multimodule.common.repository

import io.eordie.multimodule.common.security.context.getSelectionSet
import io.eordie.multimodule.common.utils.FetcherBuilder
import io.eordie.multimodule.common.utils.convert
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.babyfish.jimmer.sql.fetcher.Fetcher
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface ProjectionSupportTrait<T : Convertable<C>, C : Any, ID, F : Any> : FilterSupportTrait<T, ID, F> {

    val entityType: KClass<T>

    val dependencies: Set<KProperty1<T, *>> get() = emptySet()

    suspend fun queryById(id: ID): C? {
        return findById(id, null)?.convert()
    }

    suspend fun query(filter: F, pageable: Pageable?): Page<C> {
        val fetcher = fetcherBySelectionSet()
        return findByFilter(filter, pageable, fetcher).convert()
    }

    suspend fun queryAll(filter: F): Flow<C> {
        val fetcher = fetcherBySelectionSet()
        return findAllByFilter(filter, fetcher).map { it.convert() }
    }

    private suspend fun fetcherBySelectionSet(): Fetcher<T>? {
        val selectionSet = coroutineContext.getSelectionSet()
        return if (selectionSet == null) null else {
            val fields = selectionSet.fields.map { it.substringAfter("${Page<T>::data.name}.") }
            FetcherBuilder(entityType).newFetcher(fields + dependencies.map { it.name })
        }
    }
}
