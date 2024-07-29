package io.eordie.multimodule.common.utils

import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.SelectionSet
import org.babyfish.jimmer.sql.fetcher.Fetcher

inline fun <reified T : Any> SelectionSet.asPageFetcher(failOnUnknown: Boolean = true): Fetcher<T> {
    val entityFields = fields
        .filter { it.startsWith(Page<*>::data.name) }
        .map { it.drop(5) }
        .filter { it.isNotEmpty() }

    return fetcherImpl(entityFields, T::class.java, failOnUnknown)
}

inline fun <reified T : Any> SelectionSet.asFetcher(failOnUnknown: Boolean = true): Fetcher<T> {
    return fetcherImpl(fields, T::class.java, failOnUnknown)
}

fun <T : Any> fetcherImpl(fields: List<String>, type: Class<T>, failOnUnknown: Boolean): Fetcher<T> =
    FetcherBuilder(type).newFetcher(fields, failOnUnknown)
