package io.eordie.multimodule.example.gateway.controllers

import io.eordie.multimodule.example.contracts.basic.SchemaServiceHints
import io.eordie.multimodule.example.contracts.basic.paging.Pageable
import jakarta.inject.Singleton

@Singleton
class SchemaServiceHintsImpl : SchemaServiceHints {
    override fun pageable(pageable: Pageable): Pageable = pageable
}
