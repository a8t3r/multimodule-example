package io.eordie.multimodule.organization.config

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.phasetwo.client.PhaseTwo
import org.keycloak.admin.client.Keycloak

@Factory
class KeycloakConfig {

    @Bean
    fun keycloak(properties: KeycloakProperties): Keycloak {
        return Keycloak.getInstance(
            properties.serverUrl,
            properties.realm,
            properties.username,
            properties.password,
            properties.clientId
        )
    }

    @Bean
    fun keycloakClient(keycloak: Keycloak, properties: KeycloakProperties): PhaseTwo {
        return PhaseTwo(keycloak, properties.serverUrl)
    }
}
