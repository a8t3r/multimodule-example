package io.eordie.multimodule.common.utils

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.SelectionSet
import org.babyfish.jimmer.sql.fetcher.Fetcher
import kotlin.reflect.KClass

inline fun <reified T : Convertable<out Any>> SelectionSet.asPageFetcher(): Fetcher<T> {
    val entityFields = fields
        .filter { it.startsWith(Page<*>::data.name) }
        .map { it.drop(5) }
        .filter { it.isNotEmpty() }

    return fetcherImpl(entityFields, T::class)
}

inline fun <reified T : Convertable<out Any>> SelectionSet.asFetcher(): Fetcher<T> {
    return fetcherImpl(fields, T::class)
}

fun <T : Convertable<out Any>> fetcherImpl(fields: List<String>, type: KClass<T>): Fetcher<T> =
    FetcherBuilder(type).newFetcher(fields)
