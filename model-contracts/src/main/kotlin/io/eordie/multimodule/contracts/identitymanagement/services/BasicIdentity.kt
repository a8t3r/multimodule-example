package io.eordie.multimodule.contracts.identitymanagement.services

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.annotations.Secured
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.utils.Roles

@AutoService(Query::class)
interface BasicIdentity : Query {

    @Secured(allowAnonymous = true)
    suspend fun me(@GraphQLIgnore authentication: AuthenticationDetails?): AuthenticationDetails?

    suspend fun supportedRoles(): List<Roles> = Roles.entries.toList()
}
