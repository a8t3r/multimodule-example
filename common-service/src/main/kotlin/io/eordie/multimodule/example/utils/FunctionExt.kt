package io.eordie.multimodule.example.utils

import io.eordie.multimodule.example.contracts.basic.paging.Page
import io.eordie.multimodule.example.contracts.basic.paging.Pageable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

fun <T : Any> ((Pageable) -> Page<T>).asSequence(): Sequence<T> {
    val function = this
    var pageable = Pageable()
    return sequence {
        do {
            val page = function.invoke(pageable)
            yieldAll(page.data)
            pageable = page.pageable
        } while (pageable.cursor != null)
    }
}

fun <T : Any> ((Pageable) -> Page<T>).asFlow(): Flow<T> {
    val function = this
    var pageable = Pageable()
    return flow {
        do {
            val page = function.invoke(pageable)
            emitAll(page.data.asFlow())
            pageable = page.pageable
        } while (pageable.cursor != null)
    }
}
