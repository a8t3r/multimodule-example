package io.eordie.multimodule.common.config.kube

import io.eordie.multimodule.contracts.basic.ModuleDefinition
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.kubernetes.discovery.KubernetesServiceConfiguration
import java.util.*

@Factory
class KubernetesConfiguration {
    @Bean
    fun predefinedServiceConfigurations(): List<KubernetesServiceConfiguration> {
        return ServiceLoader.load(ModuleDefinition::class.java)
            .map { KubernetesServiceConfiguration(it.implementedBy, true).apply { setPort("rsocket") } }
    }
}
