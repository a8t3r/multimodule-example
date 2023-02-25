package io.eordie.multimodule.example.repository

import io.eordie.multimodule.example.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.example.contracts.basic.paging.Page
import io.eordie.multimodule.example.contracts.basic.paging.Pageable
import kotlinx.coroutines.flow.Flow
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.runtime.ImmutableSpi
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.query.KMutableRootQuery
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass

abstract class KBaseFactory<T : Convertable<*>, ID, F : Any>(
    entityType: KClass<T>,
    immutableType: ImmutableType
) :
    KFactoryImpl<T, ID>(entityType, immutableType),
    FilterSupportTrait<T, ID, F>,
    EntityLoader<Any, ID> where ID : Any, ID : Comparable<ID> {

    override fun load(context: CoroutineContext, ids: List<ID>): Map<ID, Any> {
        val values = findByIds(ids, context, null)
        return values.associateBy(
            { (it as ImmutableSpi).__get(idProperty.name()) as ID },
            { it.convert() }
        )
    }

    private suspend fun where(filter: F): KMutableRootQuery<T>.() -> Unit {
        val context = coroutineContext
        return {
            where(registry.toPredicates(context, filter, table))
        }
    }

    override suspend fun findIdsByFilter(filter: F): Flow<ID> = findIdsBySpecification(where(filter))
    override suspend fun findAllByFilter(filter: F): Flow<T> = findAllBySpecification(where(filter))

    override suspend fun findByFilter(filter: F, fetcher: Fetcher<T>?, pageable: Pageable): Page<T> {
        return if (fetcher == null) {
            findBySpecification(pageable, where(filter))
        } else {
            findBySpecification(fetcher, pageable, where(filter))
        }
    }
}
