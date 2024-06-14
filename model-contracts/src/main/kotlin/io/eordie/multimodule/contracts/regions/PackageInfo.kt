package io.eordie.multimodule.contracts.regions

import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.basic.ModuleDefinition

@AutoService(ModuleDefinition::class)
internal class PackageInfo : ModuleDefinition {
    override val implementedBy = "regions-service"
}
