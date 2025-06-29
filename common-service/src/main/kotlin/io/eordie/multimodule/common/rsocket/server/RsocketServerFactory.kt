package io.eordie.multimodule.common.rsocket.server

import io.eordie.multimodule.common.repository.KBaseFactory
import io.eordie.multimodule.common.rsocket.client.route.Synthesized
import io.eordie.multimodule.common.utils.GenericTypes
import io.eordie.multimodule.common.utils.like
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.Subscription
import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.ktor.network.sockets.InetSocketAddress
import io.micronaut.context.BeanLocator
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.core.io.socket.SocketUtils
import io.opentelemetry.api.OpenTelemetry
import io.rsocket.kotlin.ConnectionAcceptor
import io.rsocket.kotlin.core.RSocketServer
import io.rsocket.kotlin.transport.ktor.tcp.KtorTcpServerTransport
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KSuspendFunction2
import kotlin.reflect.full.functions

@Factory
@Requires(property = "application.rsocket.enabled", value = "true", defaultValue = "true")
class RsocketServerFactory {

    companion object {
        const val MIN_PORT = 9000
        const val MAX_PORT = 10000
    }

    private val logger = LoggerFactory.getLogger(RsocketServerFactory::class.java)

    @Singleton
    fun serverAddress(environment: Environment): InetSocketAddress {
        val port = if (environment.activeNames.contains(Environment.KUBERNETES)) MIN_PORT else {
            SocketUtils.findAvailableTcpPort(MIN_PORT, MAX_PORT)
        }

        return InetSocketAddress("0.0.0.0", port)
    }

    @Singleton
    fun bundle(
        queries: List<Query>,
        mutations: List<Mutation>,
        subscriptions: List<Subscription>
    ): Triple<List<Query>, List<Mutation>, List<Subscription>> = Triple(queries, mutations, subscriptions)

    @Singleton
    fun connectionAcceptorBuilder(
        openTelemetry: OpenTelemetry,
        resources: Triple<List<Query>, List<Mutation>, List<Subscription>>,
        beanLocator: BeanLocator,
        baseFactories: List<KBaseFactory<*, *, *, *, *>>
    ): ConnectionAcceptor {
        val loadSuspend: KSuspendFunction2<*, *, *> = EntityLoader<*, *>::load
        val entityLoaders: List<ControllerDescriptor> = baseFactories.map {
            val domainClass = GenericTypes.getTypeArguments(it, KBaseFactory::class)[1]
            val functions = it::class.functions
            ControllerDescriptor(
                it,
                EntityLoader::class,
                requireNotNull(functions.like(loadSuspend)),
                loadSuspend,
                domainClass.java.simpleName
            )
        }

        val tracer = openTelemetry.tracerBuilder("rsocket-server").build()
        return RsocketConnectionAcceptorBuilder(
            beanLocator,
            tracer,
            resources.first.filter { it !is Synthesized },
            resources.second.filter { it !is Synthesized },
            resources.third.filter { it !is Synthesized }
        )
            .addDescriptors(entityLoaders)
            .createAcceptor()
    }

    @Singleton
    fun createServer(
        localAddress: InetSocketAddress,
        connectionAcceptor: ConnectionAcceptor
    ): Job {
        val transport = KtorTcpServerTransport(EmptyCoroutineContext).target(localAddress)
        val connector = RSocketServer { }

        val job = SupervisorJob()
        val scope = CoroutineScope(
            job + CoroutineExceptionHandler { coroutineContext, throwable ->
                println("Error happened $coroutineContext: $throwable")
            }
        )

        return scope.launch {
            logger.info("started rsocket server at $localAddress")
            connector.startServer(transport, connectionAcceptor)
        }
    }
}
