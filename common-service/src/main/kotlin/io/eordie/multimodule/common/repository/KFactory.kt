package io.eordie.multimodule.common.repository

import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import kotlinx.coroutines.flow.Flow
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.query.KMutableRootQuery

@Suppress("TooManyFunctions")
interface KFactory<T : Any, S : T, ID> {

    suspend fun findByIds(ids: Collection<ID>, fetcher: Fetcher<T>? = null): Flow<T>

    suspend fun findOneBySpecification(block: KMutableRootQuery<T>.() -> Unit): T? =
        findOneBySpecification(null, block)

    suspend fun findOneBySpecification(fetcher: Fetcher<T>? = null, block: KMutableRootQuery<T>.() -> Unit): T?

    suspend fun findBySpecification(block: KMutableRootQuery<T>.() -> Unit): Page<T> =
        findBySpecification(Pageable(), block)

    suspend fun findBySpecification(pageable: Pageable, block: KMutableRootQuery<T>.() -> Unit): Page<T>
    suspend fun findBySpecification(
        fetcher: Fetcher<T>,
        pageable: Pageable,
        block: KMutableRootQuery<T>.() -> Unit
    ): Page<T>

    suspend fun findAllBySpecification(fetcher: Fetcher<T>? = null, block: KMutableRootQuery<T>.() -> Unit): Flow<T>
    suspend fun findIdsBySpecification(block: KMutableRootQuery<T>.() -> Unit): Flow<ID>

    suspend fun deleteById(id: ID): Boolean
    suspend fun deleteByIds(ids: Collection<ID>): Int
    suspend fun truncateByIds(ids: Collection<ID>): Int

    suspend fun existsById(id: ID): Boolean
    suspend fun getById(id: ID, fetcher: Fetcher<T>? = null): T
    suspend fun findById(id: ID, fetcher: Fetcher<T>? = null): T?

    suspend fun update(entity: S): T
    suspend fun update(id: ID, block: S.() -> Unit): T
    suspend fun updateIf(id: ID, block: S.() -> Boolean): Pair<T, Boolean>

    suspend fun save(block: S.() -> Unit): T
    suspend fun save(
        id: ID?,
        fetcher: Fetcher<T>? = null,
        block: (EntityState, S) -> Unit
    ): T

    suspend fun saveIf(
        id: ID?,
        fetcher: Fetcher<T>? = null,
        block: (EntityState, S) -> Boolean
    ): Pair<T, Boolean>
}
