package io.eordie.multimodule.organization.management.service

import io.eordie.multimodule.organization.management.config.KeycloakProperties
import jakarta.inject.Singleton
import org.keycloak.admin.client.Keycloak

@Singleton
class KeycloakBuilder(private val properties: KeycloakProperties) {

    fun getInstance(): Keycloak {
        val keycloak = Keycloak.getInstance(
            properties.serverUrl,
            properties.realm,
            properties.username,
            properties.password,
            properties.clientId,
            properties.clientSecret
        )

        return keycloak
    }
}
