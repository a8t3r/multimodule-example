package io.eordie.multimodule.contracts.identitymanagement.models

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable

@Serializable
@Introspected
data class OAuthToken(val accessToken: String, val refreshToken: String)
