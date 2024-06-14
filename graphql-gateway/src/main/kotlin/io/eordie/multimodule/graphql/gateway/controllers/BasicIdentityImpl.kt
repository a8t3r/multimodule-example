package io.eordie.multimodule.graphql.gateway.controllers

import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.identitymanagement.services.BasicIdentity
import jakarta.inject.Singleton

@Singleton
class BasicIdentityImpl : BasicIdentity {

    override suspend fun me(authentication: AuthenticationDetails?): AuthenticationDetails? {
        return authentication
    }
}
