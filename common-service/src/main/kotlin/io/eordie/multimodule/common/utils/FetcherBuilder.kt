package io.eordie.multimodule.common.utils

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.EntityConverter
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.meta.TargetLevel
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.fetcher.impl.FetcherImpl
import org.babyfish.jimmer.sql.fetcher.impl.FetcherImplementor
import kotlin.reflect.KClass

internal class FetcherBuilder<T : Convertable<out Any>>(private val type: KClass<T>) {

    fun newFetcher(fields: List<String>): Fetcher<T> {
        val propertiesMap = ImmutableType.get(type.java).props
        val constructorArgumentNames = EntityConverter.getIntrospection(type)
            .constructorArguments.map { it.name }
            .toSet()

        return fields.fold(FetcherImpl(type.java) as FetcherImplementor<T>) { acc, field ->
            if (field.contains('.')) acc else {
                val property = propertiesMap[field]?.takeIf { it.isScalar(TargetLevel.OBJECT) }
                    ?: propertiesMap[field + "Id"]
                    ?: propertiesMap[field.dropLast(1) + "Ids"]

                when {
                    property != null -> acc.add(property.name)
                    constructorArgumentNames.contains(field) -> error("unknown property [$field] in projection")
                    else -> acc
                }
            }
        }
    }
}
