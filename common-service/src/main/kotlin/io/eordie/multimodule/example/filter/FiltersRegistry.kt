package io.eordie.multimodule.example.filter

import io.eordie.multimodule.example.contracts.basic.filters.LiteralFilter
import io.eordie.multimodule.example.contracts.basic.filters.NumericFilter
import io.eordie.multimodule.example.contracts.basic.filters.OffsetDateTimeLiteralFilter
import io.eordie.multimodule.example.repository.FilterSupportTrait
import io.eordie.multimodule.example.repository.KBaseFactory
import io.eordie.multimodule.example.repository.and
import io.eordie.multimodule.example.repository.entity.CreatedAtIF
import io.eordie.multimodule.example.repository.entity.CreatedAtIFProps
import io.eordie.multimodule.example.repository.entity.UpdatedAtIF
import io.eordie.multimodule.example.repository.entity.UpdatedAtIFProps
import io.eordie.multimodule.example.repository.entity.VersionedEntityIF
import io.eordie.multimodule.example.repository.entity.VersionedEntityIFProps
import io.eordie.multimodule.example.repository.name
import io.micronaut.core.beans.BeanIntrospection
import io.micronaut.core.beans.BeanProperty
import io.micronaut.core.reflect.GenericTypeUtils.resolveInterfaceTypeArguments
import io.micronaut.core.reflect.GenericTypeUtils.resolveSuperTypeGenericArguments
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.time.OffsetDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.optionals.toList

@Singleton
class FiltersRegistry(
    filterSupportTraits: List<KBaseFactory<*, *, *>>
) {

    private val index = filterSupportTraits.associate {
        val interfaceTypeArguments = resolveInterfaceTypeArguments(
            it.javaClass,
            FilterSupportTrait::class.java
        )

        if (interfaceTypeArguments.isNotEmpty()) {
            interfaceTypeArguments[0] to (it to interfaceTypeArguments[1])
        } else {
            val arguments = resolveSuperTypeGenericArguments(it.javaClass, KBaseFactory::class.java)
            arguments[2] to (it to arguments[0])
        }
    }

    private inline fun <F, reified S : LiteralFilter<X>, X : Any> derived(
        introspection: BeanIntrospection<out F>,
        filter: F,
        prop: KPropExpression<X>,
        defaultProvider: () -> KNonNullExpression<Boolean>? = { null }
    ): KNonNullExpression<Boolean>? {
        val secondary: S? = introspection.getProperty(prop.name()).toList()
            .filterIsInstance<BeanProperty<F, Any>>()
            .firstNotNullOfOrNull { it.get(filter) as? S }

        return secondary?.let { prop.accept(it) } ?: defaultProvider()
    }

    fun <F : Any, T : Any> toPredicates(context: CoroutineContext, filter: F, table: KNonNullTable<T>): KNonNullExpression<Boolean>? {
        val (filterSupport, targetClass) = (index[filter::class.java] as? Pair<FilterSupportTrait<T, *, F>, Class<T>>)
            ?: error("unregistered filter type ${filter::class.simpleName}")

        val i = BeanIntrospection.getIntrospection(filter::class.java)

        val ownPredicates = filterSupport.toPredicates(context, filter, table)
        val basicPredicates = buildList {
            if (VersionedEntityIF::class.java.isAssignableFrom(targetClass)) {
                val prop = table.get<Int>(VersionedEntityIFProps.VERSION.unwrap())
                add(derived<F, NumericFilter<Int>, Int>(i, filter, prop))
            }
            if (CreatedAtIF::class.java.isAssignableFrom(targetClass)) {
                val prop = table.get<OffsetDateTime>(CreatedAtIFProps.CREATED_AT.unwrap())
                add(derived<F, OffsetDateTimeLiteralFilter, OffsetDateTime>(i, filter, prop))
            }
            if (UpdatedAtIF::class.java.isAssignableFrom(targetClass)) {
                val prop = table.get<OffsetDateTime>(UpdatedAtIFProps.UPDATED_AT.unwrap())
                add(derived<F, OffsetDateTimeLiteralFilter, OffsetDateTime>(i, filter, prop))
            }
        }.filterNotNull()

        return (basicPredicates + ownPredicates).and()
    }
}
