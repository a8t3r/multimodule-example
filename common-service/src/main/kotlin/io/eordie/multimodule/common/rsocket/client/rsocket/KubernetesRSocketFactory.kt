package io.eordie.multimodule.common.rsocket.client.rsocket

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import io.eordie.multimodule.common.rsocket.server.RsocketServerFactory
import io.eordie.multimodule.contracts.basic.ModuleDefinition
import io.micronaut.context.BeanLocator
import io.micronaut.discovery.ServiceInstance
import io.micronaut.kotlin.context.getBean
import io.micronaut.kubernetes.discovery.KubernetesDiscoveryClient
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.core.RSocketConnector
import io.rsocket.kotlin.transport.ktor.tcp.TcpClientTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.reactive.asFlow
import java.time.Duration

class KubernetesRSocketFactory(beanLocator: BeanLocator) : RSocketLocalFactory {
    companion object {
        private const val EXPIRATION_MINUTES = 10L
        private const val MAX_RETRIES = 3
    }

    private val client by lazy { beanLocator.getBean<KubernetesDiscoveryClient>() }

    private val rsocketCache = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(EXPIRATION_MINUTES))
        .build(object : CacheLoader<String, Deferred<RSocket>>() {
            override fun load(hostAddress: String): Deferred<RSocket> {
                val transport = TcpClientTransport(hostAddress, RsocketServerFactory.MIN_PORT)
                val connector = RSocketConnector {
                    reconnectable(5)
                }
                return CoroutineScope(Dispatchers.IO).async { connector.connect(transport) }
            }
        })

    private suspend fun tryGet(address: String) = kotlin.runCatching { rsocketCache.get(address).await() }.getOrNull()
    private fun noImplementation(module: ModuleDefinition): Nothing =
        error("no service implementation [${module.implementedBy}] found")

    private suspend fun rsocketByAddress(address: String, module: ModuleDefinition): RSocket {
        var retries = MAX_RETRIES
        var socket = tryGet(address)?.takeIf { it.isActive }
        while (socket == null && retries-- > 0) {
            rsocketCache.refresh(address)
            socket = tryGet(address)?.takeIf { it.isActive }
        }

        return socket ?: noImplementation(module)
    }

    private suspend fun instances(module: ModuleDefinition): List<ServiceInstance> {
        return client.getInstances(module.implementedBy).asFlow().toList().flatten()
            .takeIf { it.isNotEmpty() } ?: noImplementation(module)
    }

    override suspend fun rsocket(module: ModuleDefinition): RSocket {
        val instance = instances(module).random()
        return rsocketByAddress(instance.host, module)
    }

    override suspend fun rsockets(module: ModuleDefinition): List<RSocket> {
        return instances(module).map { rsocketByAddress(it.host, module) }
    }
}
