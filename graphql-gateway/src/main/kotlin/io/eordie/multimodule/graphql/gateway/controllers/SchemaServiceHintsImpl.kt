package io.eordie.multimodule.graphql.gateway.controllers

import io.eordie.multimodule.contracts.basic.SchemaServiceHints
import io.eordie.multimodule.contracts.basic.paging.Pageable
import jakarta.inject.Singleton

@Singleton
class SchemaServiceHintsImpl : SchemaServiceHints {
    override fun pageable(pageable: Pageable): Pageable = pageable
}
