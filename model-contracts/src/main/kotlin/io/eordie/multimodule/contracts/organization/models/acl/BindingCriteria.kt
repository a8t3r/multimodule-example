package io.eordie.multimodule.contracts.organization.models.acl

import io.eordie.multimodule.contracts.utils.UuidStr
import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

interface BindingCriterion

@Introspected
@Serializable
data class GlobalCriterion(val includeAll: Boolean) : BindingCriterion

@Introspected
@Serializable
data class ByRegionCriterion(val regionId: Long) : BindingCriterion

@Introspected
@Serializable
data class ByFarmCriterion(val farmId: UuidStr, val fieldIds: List<UuidStr>? = null) : BindingCriterion
