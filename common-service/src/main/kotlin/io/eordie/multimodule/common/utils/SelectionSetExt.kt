package io.eordie.multimodule.common.utils

import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.SelectionSet
import org.babyfish.jimmer.sql.fetcher.Fetcher

inline fun <reified T : Any> SelectionSet.asPageFetcher(): Fetcher<T> {
    val entityFields = fields
        .filter { it.startsWith(Page<*>::data.name) }
        .map { it.drop(5) }
        .filter { it.isNotEmpty() }

    return fetcherImpl(entityFields, T::class.java)
}

inline fun <reified T : Any> SelectionSet.asFetcher(): Fetcher<T> {
    return fetcherImpl(fields, T::class.java)
}

fun <T : Any> fetcherImpl(fields: List<String>, type: Class<T>): Fetcher<T> = FetcherBuilder(type).newFetcher(fields)
