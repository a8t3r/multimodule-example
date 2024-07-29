package io.eordie.multimodule.common.utils

import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.meta.TargetLevel
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.fetcher.impl.FetcherImpl
import org.babyfish.jimmer.sql.fetcher.impl.FetcherImplementor

class FetcherBuilder<T>(private val type: Class<T>) {

    fun newFetcher(fields: List<String>, failOnUnknown: Boolean): Fetcher<T> {
        val propertiesMap = ImmutableType.get(type).selectableScalarProps
        return fields.fold(FetcherImpl(type) as FetcherImplementor<T>) { acc, field ->
            if (field.contains('.')) acc else {
                val property = propertiesMap[field]?.takeIf { it.isScalar(TargetLevel.OBJECT) }
                    ?: propertiesMap[field + "Id"]
                    ?: propertiesMap[field.dropLast(1) + "Ids"]

                when {
                    property != null -> acc.add(property.name)
                    failOnUnknown -> error("unknown property [$field] in projection")
                    else -> acc
                }
            }
        }
    }
}
