package io.eordie.multimodule.example.gateway.controllers

import io.eordie.multimodule.example.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.example.contracts.identitymanagement.services.BasicIdentity
import jakarta.inject.Singleton

@Singleton
class BasicIdentityImpl : BasicIdentity {

    override suspend fun me(authentication: AuthenticationDetails?): AuthenticationDetails? {
        return authentication
    }
}
