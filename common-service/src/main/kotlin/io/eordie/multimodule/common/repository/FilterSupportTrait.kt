package io.eordie.multimodule.common.repository

import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import kotlinx.coroutines.flow.Flow
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable

interface FilterSupportTrait<T : Any, ID, F : Any> {

    fun ResourceAcl.toPredicates(filter: F, table: KNonNullTable<T>): List<KNonNullExpression<Boolean>> =
        emptyList()

    suspend fun findById(id: ID, fetcher: Fetcher<T>? = null): T?
    suspend fun findIdsByFilter(filter: F): Flow<ID>
    suspend fun findAllByFilter(filter: F, fetcher: Fetcher<T>? = null): Flow<T>
    suspend fun findByFilter(filter: F, pageable: Pageable? = null, fetcher: Fetcher<T>? = null): Page<T>
}
