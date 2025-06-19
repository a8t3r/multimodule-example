package io.eordie.multimodule.common.config

import io.eordie.multimodule.contracts.basic.ModuleDefinition
import io.eordie.multimodule.contracts.utils.safeCast
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.kubernetes.KubernetesConfiguration
import io.micronaut.kubernetes.client.informer.SharedIndexInformerFactory
import io.micronaut.kubernetes.discovery.KubernetesServiceConfiguration
import jakarta.inject.Singleton
import java.lang.reflect.Proxy
import java.util.*
import javax.naming.OperationNotSupportedException

@Factory
class KubernetesConfig {

    @Singleton
    @Requires(notEnv = [Environment.KUBERNETES])
    fun kubernetesConfiguration(): KubernetesConfiguration = KubernetesConfiguration { null }

    @Singleton
    @Requires(notEnv = [Environment.KUBERNETES])
    fun indexInformerFactory(): SharedIndexInformerFactory {
        val contract = SharedIndexInformerFactory::class.java
        return safeCast(
            Proxy.newProxyInstance(contract.classLoader, arrayOf(contract)) { _, method, arguments ->
                throw OperationNotSupportedException()
            }
        )
    }

    @Bean
    fun predefinedServiceConfigurations(): List<KubernetesServiceConfiguration> {
        return ServiceLoader.load(ModuleDefinition::class.java)
            .map { KubernetesServiceConfiguration(it.implementedBy, true).apply { setPort("rsocket") } }
    }
}
