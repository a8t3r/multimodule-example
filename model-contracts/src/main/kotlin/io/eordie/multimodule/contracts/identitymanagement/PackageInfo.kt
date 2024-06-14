package io.eordie.multimodule.contracts.identitymanagement

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.basic.ModuleDefinition

@AutoService(ModuleDefinition::class)
internal class PackageInfo : ModuleDefinition {
    override val implementedBy = "graphql-gateway"
}
