package io.eordie.multimodule.common.utils

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.EntityConverter
import io.eordie.multimodule.contracts.basic.PermissionAware
import kotlinx.serialization.Required
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.meta.TargetLevel
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.fetcher.impl.FetcherImpl
import org.babyfish.jimmer.sql.fetcher.impl.FetcherImplementor
import kotlin.reflect.KClass

internal class FetcherBuilder<T : Convertable<out Any>>(private val type: KClass<T>) {

    fun newFetcher(fields: List<String>): Fetcher<T> {
        val propertiesMap = ImmutableType.get(type.java).props
        val constructorArguments = EntityConverter.getIntrospection(type).constructorArguments
        val constructorArgumentNames = constructorArguments.map { it.name }.toSet()

        val initial = (FetcherImpl(type.java) as FetcherImplementor<T>).apply {
            constructorArguments
                .filter { it.isAnnotationPresent(Required::class.java) }
                .forEach { add(it.name) }
        }

        return fields.fold(initial) { acc, field ->
            if (field.contains('.')) acc else {
                val property = propertiesMap[field]?.takeIf { it.isScalar(TargetLevel.OBJECT) }
                    ?: propertiesMap[field + "Id"]
                    ?: propertiesMap[field.dropLast(1) + "Ids"]

                when {
                    property != null -> acc.add(property.name)
                    field == PermissionAware<*>::permissions.name -> acc
                    constructorArgumentNames.contains(field) -> error("unknown property [$field] in projection")
                    else -> acc
                }
            }
        }
    }
}
