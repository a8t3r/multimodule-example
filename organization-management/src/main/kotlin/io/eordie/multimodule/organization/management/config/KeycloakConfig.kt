package io.eordie.multimodule.organization.management.config

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.phasetwo.client.OrganizationsResource
import io.phasetwo.client.PhaseTwo
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder

@Factory
class KeycloakConfig {

    @Bean
    fun organizationResource(phaseTwo: PhaseTwo, properties: KeycloakProperties): OrganizationsResource {
        return phaseTwo.organizations(properties.realm)
    }

    @Bean
    fun adminPhaseTwo(keycloak: Keycloak, properties: KeycloakProperties): PhaseTwo {
        return PhaseTwo(keycloak, properties.serverUrl)
    }

    @Bean
    fun adminKeycloak(properties: KeycloakProperties): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(properties.serverUrl)
            .realm(properties.realm)
            .grantType(OAuth2Constants.PASSWORD)
            .username(properties.username)
            .password(properties.password)
            .clientId(properties.clientId)
            .clientSecret(properties.clientSecret)
            .build()
    }
}
