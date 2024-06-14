package io.eordie.multimodule.contracts.identitymanagement.models

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import io.eordie.multimodule.contracts.utils.UuidStr
import kotlinx.serialization.Serializable

@GraphQLIgnore
@Serializable
class CurrentOrganization(val id: UuidStr) {
    companion object {
        fun of(auth: AuthenticationDetails) = CurrentOrganization(requireNotNull(auth.currentOrganizationId))
    }
}
