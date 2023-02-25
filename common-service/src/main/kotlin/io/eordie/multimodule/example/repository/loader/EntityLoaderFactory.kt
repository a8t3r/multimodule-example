package io.eordie.multimodule.example.repository.loader

import io.eordie.multimodule.example.contracts.basic.loader.EntityLoader
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.inject.ArgumentInjectionPoint

@Factory
class EntityLoaderFactory {

    @Bean
    fun buildLoader(
        genericEntityLoader: GenericEntityLoader,
        injection: ArgumentInjectionPoint<*, *>
    ): EntityLoader<*, *> {
        val entityType = injection.asArgument().getTypeVariable("T").get().type
        return genericEntityLoader.createLoader(entityType)
    }
}
