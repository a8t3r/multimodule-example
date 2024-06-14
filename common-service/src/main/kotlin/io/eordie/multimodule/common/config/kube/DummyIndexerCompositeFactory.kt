package io.eordie.multimodule.common.config.kube

import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.kubernetes.discovery.informer.IndexerCompositeFactory
import jakarta.inject.Singleton

@Singleton
@Requires(notEnv = [ Environment.KUBERNETES ])
@Replaces(IndexerCompositeFactory::class)
class DummyIndexerCompositeFactory : IndexerCompositeFactory(null)
