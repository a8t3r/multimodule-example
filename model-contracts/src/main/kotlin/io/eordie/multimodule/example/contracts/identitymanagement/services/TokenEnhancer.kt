package io.eordie.multimodule.example.contracts.identitymanagement.services

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.google.auto.service.AutoService
import io.eordie.multimodule.example.contracts.Mutation
import io.micronaut.core.type.MutableHeaders
import java.util.*

@AutoService(Mutation::class)
interface TokenEnhancer : Mutation {

    suspend fun switchOrganization(@GraphQLIgnore headers: MutableHeaders, organizationId: UUID): Boolean
}
