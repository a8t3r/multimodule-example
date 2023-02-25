package io.eordie.multimodule.example.contracts.identitymanagement.models

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import java.util.*

@JvmInline
@GraphQLIgnore
value class CurrentOrganization(val id: UUID) {
    companion object {
        fun of(auth: AuthenticationDetails) = CurrentOrganization(requireNotNull(auth.currentOrganizationId))
    }
}
