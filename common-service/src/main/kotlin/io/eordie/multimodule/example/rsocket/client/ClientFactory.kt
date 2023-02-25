package io.eordie.multimodule.example.rsocket.client

import io.eordie.multimodule.example.contracts.Mutation
import io.eordie.multimodule.example.contracts.Query
import io.eordie.multimodule.example.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.example.repository.KBaseFactory
import io.eordie.multimodule.example.rsocket.client.invocation.Synthesized
import io.eordie.multimodule.example.rsocket.server.RsocketServerFactory
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Factory
import io.micronaut.discovery.metadata.ServiceInstanceMetadataContributor
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Factory
class ClientFactory {

    @Inject
    lateinit var context: ApplicationContext

    private fun MutableMap<String, String>.add(key: String) {
        this["[$key]"] = "1"
    }

    @Singleton
    fun serviceInstanceMetadataContributor(
        queries: List<Query>,
        mutations: List<Mutation>,
        baseFactories: List<KBaseFactory<*, *, *>>
    ): ServiceInstanceMetadataContributor {
        return ServiceInstanceMetadataContributor { _, metadata ->
            (queries + mutations)
                .filter { it !is Synthesized }
                .mapNotNull { it::class.getServiceInterface()?.simpleName }
                .forEach { metadata.add(it) }

            baseFactories.map {
                val prefix = requireNotNull(it.entityType.simpleName).removeSuffix("Model")
                metadata.add("${prefix}${EntityLoader::class.simpleName}")
            }

            metadata["rsocket_port"] = RsocketServerFactory.availableTcpPort.toString()
        }
    }
}
