package io.eordie.multimodule.common.repository.loader

import io.eordie.multimodule.contracts.basic.loader.EntityLoader
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
        val idType = injection.asArgument().getTypeVariable("ID").get().type
        val entityType = injection.asArgument().getTypeVariable("T").get().type
        return genericEntityLoader.createLoader(idType, entityType)
    }
}
