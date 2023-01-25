package io.eordie.multimodule.example.gateway.controllers

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.example.contracts.identitymanagement.services.BasicIdentity
import io.micronaut.http.HttpAttributes
import io.micronaut.security.authentication.Authentication
import jakarta.inject.Singleton

@Singleton
class BasicIdentityImpl : BasicIdentity {

    override suspend fun me(env: DataFetchingEnvironment): AuthenticationDetails {
        val auth = env.graphQlContext.get<Authentication?>(HttpAttributes.PRINCIPAL)
        return if (auth == null) AuthenticationDetails(null) else {
            val attributes = auth.attributes

            AuthenticationDetails(
                userId = auth.name,
                username = attributes["username"] as String,
                roles = auth.roles.toList(),
                active = attributes["active"] as Boolean,
                emailVerified = attributes["emailVerified"] as Boolean
            )
        }
    }
}
