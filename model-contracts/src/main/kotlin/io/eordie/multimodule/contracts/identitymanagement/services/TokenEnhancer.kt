package io.eordie.multimodule.contracts.identitymanagement.services

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.google.auto.service.AutoService
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.identitymanagement.models.OAuthToken
import io.micronaut.core.type.MutableHeaders
import java.util.*

@AutoService(Mutation::class)
interface TokenEnhancer : Mutation {

    suspend fun switchOrganization(
        @GraphQLIgnore headers: MutableHeaders,
        @GraphQLIgnore token: OAuthToken,
        organizationId: UUID
    ): Boolean
}
