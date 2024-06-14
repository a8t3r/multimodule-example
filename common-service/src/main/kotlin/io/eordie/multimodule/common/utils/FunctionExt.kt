package io.eordie.multimodule.common.utils

import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

fun <P1, P2, R> ((P1, P2) -> R).curried(p1: P1): (P2) -> R = { p2: P2 -> this(p1, p2) }
suspend fun <P1, P2, R> (suspend (P1, P2) -> R).curried(p1: P1): suspend (P2) -> R = { p2: P2 -> this(p1, p2) }

fun <P1, P2, P3, R> ((P1, P2, P3) -> R).curried(p1: P1, p2: P2): (P3) -> R = { p3: P3 -> this(p1, p2, p3) }
suspend fun <P1, P2, P3, R> (suspend (P1, P2, P3) -> R).curried(p1: P1, p2: P2): suspend (P3) -> R =
    { p3: P3 -> this(p1, p2, p3) }

fun <P1, P2, P3, P4, R> ((P1, P2, P3, P4) -> R).curried(p1: P1, p2: P2, p3: P3): (P4) -> R =
    { p4: P4 -> this(p1, p2, p3, p4) }

suspend fun <P1, P2, P3, P4, R> (suspend (P1, P2, P3, P4) -> R).curried(p1: P1, p2: P2, p3: P3): suspend (P4) -> R =
    { p4: P4 -> this(p1, p2, p3, p4) }

fun <P1, P2, P3, P4, P5, R> ((P1, P2, P3, P4, P5) -> R).curried(p1: P1, p2: P2, p3: P3, p4: P4): (P5) -> R =
    { p5: P5 -> this(p1, p2, p3, p4, p5) }

suspend fun <P1, P2, P3, P4, P5, R> (suspend (P1, P2, P3, P4, P5) -> R).curried(
    p1: P1,
    p2: P2,
    p3: P3,
    p4: P4
): suspend (P5) -> R = { p5: P5 -> this(p1, p2, p3, p4, p5) }

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

private fun <T : Any> buildFlow(function: suspend (Pageable) -> Page<T>): Flow<T> {
    var pageable = Pageable()
    return flow {
        do {
            val page = function.invoke(pageable)
            emitAll(page.data.asFlow())
            pageable = page.pageable
        } while (pageable.cursor != null)
    }
}

fun <T : Any> ((Pageable) -> Page<T>).asFlow(): Flow<T> {
    return buildFlow { this.invoke(it) }
}

fun <T : Any> (suspend (Pageable) -> Page<T>).asFlow(): Flow<T> {
    return buildFlow { this.invoke(it) }
}
