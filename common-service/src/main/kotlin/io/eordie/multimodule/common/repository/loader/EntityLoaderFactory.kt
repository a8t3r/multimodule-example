package io.eordie.multimodule.common.repository.loader

import io.eordie.multimodule.common.utils.typeArguments
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
        val (idType, entityType) = injection.typeArguments<Any, Any>("ID", "T")
        return genericEntityLoader.createLoader(idType, entityType)
    }
}
