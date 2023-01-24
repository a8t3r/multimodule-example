package io.eordie.multimodule.example.contracts.identitymanagement.services

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.example.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.example.contracts.utils.IS_ANONYMOUS
import io.eordie.multimodule.example.contracts.utils.Query
import io.micronaut.security.annotation.Secured

interface BasicIdentity : Query {

    @Secured(IS_ANONYMOUS)
    suspend fun me(env: DataFetchingEnvironment): AuthenticationDetails
}
