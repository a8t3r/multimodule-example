package io.eordie.multimodule.organization.service

import io.eordie.multimodule.example.contracts.identitymanagement.models.OAuthToken
import io.eordie.multimodule.organization.config.KeycloakProperties
import io.phasetwo.client.OrganizationsResource
import io.phasetwo.client.PhaseTwo
import jakarta.inject.Singleton
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource

@Singleton
class KeycloakProvider(
    private val properties: KeycloakProperties
) {

    fun buildOrganizationsClient(token: OAuthToken): OrganizationsResource {
        return buildInternalsClient(token).organizations(properties.realm)
    }

    private fun buildInternalsClient(token: OAuthToken): PhaseTwo {
        return PhaseTwo(buildKeycloakClient(token), properties.serverUrl)
    }

    fun buildRealmClient(token: OAuthToken): RealmResource {
        return buildKeycloakClient(token).realm(properties.realm)
    }

    private fun buildKeycloakClient(token: OAuthToken): Keycloak {
        return Keycloak.getInstance(
            properties.serverUrl,
            properties.realm,
            properties.clientId,
            token.accessToken
        )
    }
}
