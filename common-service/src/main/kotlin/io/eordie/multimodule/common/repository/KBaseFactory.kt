package io.eordie.multimodule.common.repository

import io.eordie.multimodule.common.repository.event.EventPublisher
import io.eordie.multimodule.common.repository.event.ObjectDiffer.difference
import io.eordie.multimodule.common.repository.ext.name
import io.eordie.multimodule.contracts.basic.event.MutationEvent
import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.babyfish.jimmer.runtime.ImmutableSpi
import org.babyfish.jimmer.sql.event.EntityListener
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.query.KMutableRootQuery
import kotlin.reflect.KClass

abstract class KBaseFactory<T : Convertable<C>, C : Any, ID, F : Any>(
    entityType: KClass<T>
) :
    KFactoryImpl<T, ID>(entityType),
    FilterSupportTrait<T, ID, F>,
    EntityLoader<C, ID> where ID : Any, ID : Comparable<ID> {

    @Inject
    private lateinit var eventPublisher: EventPublisher

    override val entityListener: EntityListener<T> = EntityListener {
        val (old, new) = it.oldEntity?.convert() to it.newEntity?.convert()
        val event = MutationEvent(it.id.toString(), old, new, difference(old, new))

        if (!event.isUpdated() || event.difference?.hasAnyChanges() == true) {
            val convertedType = requireNotNull((old ?: new))::class
            val affectedBy = (it.connection as? ConnectionWrapper)?.context
            eventPublisher.publish(convertedType, requireNotNull(affectedBy), event)
        }
    }

    override suspend fun load(ids: List<ID>): Map<ID, C> {
        val values = internalFindByIds(ids, null)
        return values.associateBy(
            { (it as ImmutableSpi).__get(idProperty.name()) as ID },
            { it.convert() }
        )
    }

    private suspend fun where(filter: F): KMutableRootQuery<T>.() -> Unit {
        val acl = resourceAcl()
        return {
            where(registry.toPredicates(acl, filter, table))
        }
    }

    override suspend fun findIdsByFilter(filter: F): Flow<ID> = findIdsBySpecification(where(filter))
    override suspend fun findAllByFilter(filter: F, fetcher: Fetcher<T>?): Flow<T> =
        findAllBySpecification(fetcher, where(filter))

    override suspend fun findByFilter(filter: F, pageable: Pageable?, fetcher: Fetcher<T>?): Page<T> {
        val pageableBy = pageable ?: Pageable()
        return if (fetcher == null) {
            findBySpecification(pageableBy, where(filter))
        } else {
            findBySpecification(fetcher, pageableBy, where(filter))
        }
    }
}
