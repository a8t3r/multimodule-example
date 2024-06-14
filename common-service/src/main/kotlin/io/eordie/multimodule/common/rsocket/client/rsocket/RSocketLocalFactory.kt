package io.eordie.multimodule.common.rsocket.client.rsocket

import io.eordie.multimodule.contracts.basic.ModuleDefinition
import io.rsocket.kotlin.RSocket

interface RSocketLocalFactory {
    suspend fun rsocket(module: ModuleDefinition): RSocket
    suspend fun rsockets(module: ModuleDefinition): List<RSocket>
}
