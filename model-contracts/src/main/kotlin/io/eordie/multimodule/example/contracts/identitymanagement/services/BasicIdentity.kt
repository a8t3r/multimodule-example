package io.eordie.multimodule.example.contracts.identitymanagement.services

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.google.auto.service.AutoService
import io.eordie.multimodule.example.contracts.Query
import io.eordie.multimodule.example.contracts.annotations.Secured
import io.eordie.multimodule.example.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.example.contracts.utils.Roles

@AutoService(Query::class)
interface BasicIdentity : Query {

    @Secured(allowAnonymous = true)
    suspend fun me(@GraphQLIgnore authentication: AuthenticationDetails?): AuthenticationDetails?

    suspend fun supportedRoles(): List<String> {
        return Roles.entries.map { it.humanName() }.toList()
    }
}
