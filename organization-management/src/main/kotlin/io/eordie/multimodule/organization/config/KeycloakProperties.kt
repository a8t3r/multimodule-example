package io.eordie.multimodule.organization.config

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("keycloak")
data class KeycloakProperties @ConfigurationInject constructor(
    val serverUrl: String,
    val realm: String,
    val username: String,
    val password: String,
    val clientId: String,
    val clientSecret: String
)
