package io.eordie.multimodule.common.rsocket.client.rsocket

import io.eordie.multimodule.contracts.basic.ModuleDefinition
import io.ktor.network.sockets.*
import io.micronaut.context.BeanLocator
import io.micronaut.kotlin.context.getBean
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.core.RSocketConnector
import io.rsocket.kotlin.transport.ktor.tcp.TcpClientTransport

class LocalRSocketFactory(private val beanLocator: BeanLocator) : RSocketLocalFactory {
    private lateinit var connection: RSocket

    override suspend fun rsocket(module: ModuleDefinition): RSocket {
        if (!this::connection.isInitialized) {
            val address = beanLocator.getBean<InetSocketAddress>()
            val transport = TcpClientTransport("localhost", address.port)
            val connector = RSocketConnector {
                reconnectable(5)
            }
            connection = connector.connect(transport)
        }

        return connection
    }

    override suspend fun rsockets(module: ModuleDefinition): List<RSocket> {
        return listOf(rsocket(module))
    }
}
