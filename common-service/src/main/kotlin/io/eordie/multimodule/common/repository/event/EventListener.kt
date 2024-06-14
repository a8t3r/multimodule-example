package io.eordie.multimodule.common.repository.event

import io.eordie.multimodule.contracts.basic.event.MutationEvent

interface EventListener<T> {
    suspend fun onEvent(event: MutationEvent<T>)
}
