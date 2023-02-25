package io.eordie.multimodule.example.repository

import io.eordie.multimodule.example.contracts.basic.paging.Page
import io.eordie.multimodule.example.contracts.basic.paging.Pageable
import kotlinx.coroutines.flow.Flow
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import kotlin.coroutines.CoroutineContext

interface FilterSupportTrait<T : Any, ID, F : Any> {

    fun toPredicates(context: CoroutineContext, filter: F, table: KNonNullTable<T>): List<KNonNullExpression<Boolean>> =
        emptyList()

    suspend fun findIdsByFilter(filter: F): Flow<ID>
    suspend fun findAllByFilter(filter: F): Flow<T>
    suspend fun findByFilter(filter: F, fetcher: Fetcher<T>? = null, pageable: Pageable = Pageable()): Page<T>
}
