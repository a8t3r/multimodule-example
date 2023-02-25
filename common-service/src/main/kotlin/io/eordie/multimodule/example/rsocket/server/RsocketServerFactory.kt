package io.eordie.multimodule.example.rsocket.server

import io.eordie.multimodule.example.contracts.Mutation
import io.eordie.multimodule.example.contracts.Query
import io.eordie.multimodule.example.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.example.repository.KBaseFactory
import io.eordie.multimodule.example.rsocket.client.invocation.Synthesized
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import io.micronaut.context.BeanLocator
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.core.io.socket.SocketUtils
import io.opentelemetry.api.OpenTelemetry
import io.rsocket.kotlin.core.RSocketServer
import io.rsocket.kotlin.transport.ktor.tcp.TcpServer
import io.rsocket.kotlin.transport.ktor.tcp.TcpServerTransport
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.slf4j.LoggerFactory
import kotlin.reflect.KFunction3
import kotlin.reflect.KSuspendFunction2

@Factory
@Requires(property = "application.rsocket.enabled", value = "true", defaultValue = "true")
class RsocketServerFactory {

    companion object {
        private const val MIN_PORT = 9000
        private const val MAX_PORT = 10000

        val availableTcpPort: Int by lazy { SocketUtils.findAvailableTcpPort(MIN_PORT, MAX_PORT) }
    }

    private val logger = LoggerFactory.getLogger(RsocketServerFactory::class.java)

    @Singleton
    fun createServer(
        openTelemetry: OpenTelemetry,
        beanLocator: BeanLocator,
        queries: List<Query>,
        mutations: List<Mutation>,
        baseFactories: List<KBaseFactory<*, *, *>>
    ): TcpServer {
        val localAddress = InetSocketAddress("0.0.0.0", availableTcpPort)
        val transport = TcpServerTransport(localAddress, DefaultBufferPool()) { }
        val connector = RSocketServer {
        }

        val job = SupervisorJob()
        val scope = CoroutineScope(
            job + CoroutineExceptionHandler { coroutineContext, throwable ->
                println("Error happened $coroutineContext: $throwable")
            }
        )

        val load: KFunction3<*, *, *, *> = EntityLoader<*, *>::load
        val loadSuspend: KSuspendFunction2<*, *, *> = EntityLoader<*, *>::load
        val entityLoaders: List<ControllerDescriptor> = baseFactories.flatMap {
            val prefix = requireNotNull(it.entityType.simpleName).removeSuffix("Model")
            listOf(
                ControllerDescriptor(it, EntityLoader::class, load, prefix),
                ControllerDescriptor(it, EntityLoader::class, loadSuspend, prefix)
            )
        }

        val tracer = openTelemetry.tracerBuilder("rsocket-server").build()
        val controllers = (queries + mutations).filter { it !is Synthesized }
        val acceptor = RsocketConnectionAcceptorBuilder(beanLocator, tracer, controllers)
            .addDescriptors(entityLoaders)
            .createAcceptor()

        logger.info("started rsocket server at $localAddress")
        return connector.bindIn(scope, transport, acceptor)
    }
}
