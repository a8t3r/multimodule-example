package io.eordie.multimodule.contracts.basic.loader

import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

suspend fun <T : Any, ID> EntityLoader<T, ID>.loadOne(context: CoroutineContext, id: ID?): T? {
    return if (id == null) null else {
        withContext(context) {
            load(listOf(id))[id]
        }
    }
}
