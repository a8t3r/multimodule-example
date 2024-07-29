package io.eordie.multimodule.common.utils

import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.SelectionSet
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.meta.TargetLevel
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.fetcher.impl.FetcherImpl
import org.babyfish.jimmer.sql.fetcher.impl.FetcherImplementor

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

fun <T : Any> fetcherImpl(fields: List<String>, type: Class<T>): Fetcher<T> {
    val immutableType = ImmutableType.get(type)
    val propertiesMap = immutableType.props

    return fields.fold(FetcherImpl(type) as FetcherImplementor<T>) { acc, field ->
        if (field.contains('.')) acc else {
            val property = propertiesMap[field]?.takeIf { it.isScalar(TargetLevel.OBJECT) }
                ?: propertiesMap[field + "Id"]
                ?: propertiesMap[field.dropLast(1) + "Ids"]
                ?: error("unknown property [$field] in projection")

            acc.add(property.name)
        }
    }
}
