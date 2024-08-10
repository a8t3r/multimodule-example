package io.eordie.multimodule.common.filter

import io.eordie.multimodule.common.repository.FilterSupportTrait
import io.eordie.multimodule.common.repository.KBaseFactory
import io.eordie.multimodule.common.repository.entity.CreatedAtIF
import io.eordie.multimodule.common.repository.entity.CreatedAtIFProps
import io.eordie.multimodule.common.repository.entity.UpdatedAtIF
import io.eordie.multimodule.common.repository.entity.UpdatedAtIFProps
import io.eordie.multimodule.common.repository.entity.VersionedEntityIF
import io.eordie.multimodule.common.repository.entity.VersionedEntityIFProps
import io.eordie.multimodule.common.repository.ext.and
import io.eordie.multimodule.common.repository.ext.name
import io.eordie.multimodule.common.utils.GenericTypes
import io.eordie.multimodule.contracts.basic.filters.LiteralFilter
import io.eordie.multimodule.contracts.basic.filters.NumericFilter
import io.eordie.multimodule.contracts.basic.filters.OffsetDateTimeLiteralFilter
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.contracts.utils.getIntrospection
import io.micronaut.core.beans.BeanIntrospection
import io.micronaut.core.beans.BeanProperty
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.time.OffsetDateTime
import kotlin.jvm.optionals.toList
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

@Singleton
class FiltersRegistry(
    filterSupportTraits: List<KBaseFactory<*, *, *, *>>
) {

    private val index = filterSupportTraits.associate {
        val arguments = GenericTypes.getTypeArguments(it, KBaseFactory::class)
        arguments[3] to (it to arguments[0])
    }

    private inline fun <F, reified S : LiteralFilter<X>, X : Any> derived(
        introspection: BeanIntrospection<out F>,
        filter: F,
        prop: KPropExpression<X>
    ): KNonNullExpression<Boolean>? {
        val secondary: S? = introspection.getProperty(prop.name()).toList()
            .filterIsInstance<BeanProperty<F, Any>>()
            .firstNotNullOfOrNull { it.get(filter) as? S }

        return secondary?.let { prop.accept(it) }
    }

    fun <F : Any, T : Any> toPredicates(acl: ResourceAcl, filter: F, table: KNonNullTable<T>): KNonNullExpression<Boolean>? {
        val (filterSupport, targetClass) = (index[filter::class] as? Pair<FilterSupportTrait<T, *, F>, KClass<T>>)
            ?: error("unregistered filter type ${filter::class.simpleName}")

        val i = getIntrospection<F>(filter::class)
        val ownPredicates = with(filterSupport) {
            acl.toPredicates(filter, table) + acl.listPredicates(table)
        }

        val basicPredicates = buildList {
            if (VersionedEntityIF::class.isSuperclassOf(targetClass)) {
                val prop = table.get<Int>(VersionedEntityIFProps.VERSION.unwrap())
                add(derived<F, NumericFilter<Int>, Int>(i, filter, prop))
            }
            if (CreatedAtIF::class.isSuperclassOf(targetClass)) {
                val prop = table.get<OffsetDateTime>(CreatedAtIFProps.CREATED_AT.unwrap())
                add(derived<F, OffsetDateTimeLiteralFilter, OffsetDateTime>(i, filter, prop))
            }
            if (UpdatedAtIF::class.isSuperclassOf(targetClass)) {
                val prop = table.get<OffsetDateTime>(UpdatedAtIFProps.UPDATED_AT.unwrap())
                add(derived<F, OffsetDateTimeLiteralFilter, OffsetDateTime>(i, filter, prop))
            }
        }.filterNotNull()

        return (basicPredicates + ownPredicates).and()
    }
}
