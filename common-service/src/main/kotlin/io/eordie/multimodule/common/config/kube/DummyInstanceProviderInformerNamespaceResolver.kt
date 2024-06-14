package io.eordie.multimodule.common.config.kube

import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.kubernetes.discovery.informer.InstanceProviderInformerNamespaceResolver
import jakarta.inject.Singleton

@Singleton
@Requires(notEnv = [ Environment.KUBERNETES ])
@Replaces(InstanceProviderInformerNamespaceResolver::class)
class DummyInstanceProviderInformerNamespaceResolver :
    InstanceProviderInformerNamespaceResolver(null, null, null)
