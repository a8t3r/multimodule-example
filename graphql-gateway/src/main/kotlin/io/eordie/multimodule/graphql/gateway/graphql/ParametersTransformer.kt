package io.eordie.multimodule.graphql.gateway.graphql

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.identitymanagement.models.OAuthToken
import io.eordie.multimodule.graphql.gateway.security.KeycloakRefreshAuthenticator
import io.micronaut.core.type.Headers
import io.micronaut.core.type.MutableHeaders
import jakarta.inject.Singleton
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KParameter

@Singleton
class ParametersTransformer(
    private val authenticator: KeycloakRefreshAuthenticator
) {

    fun mapParameterToValue(param: KParameter, environment: DataFetchingEnvironment): Pair<KParameter, Any?>? {
        val classifier = param.type.classifier
        return when (classifier) {
            Headers::class -> { param to environment.graphQlContext.get(ContextKeys.HEADERS) }
            MutableHeaders::class -> { param to environment.graphQlContext.get(ContextKeys.RESPONSE_HEADERS) }
            DataFetchingEnvironment::class -> param to environment
            AuthenticationDetails::class -> {
                val value = environment.graphQlContext.authenticationDetailsOrNull()
                    ?: if (param.type.isMarkedNullable) null else {
                        error("method can't accept nullable authentication details")
                    }

                param to value
            }
            CoroutineContext::class -> param to null
            Continuation::class -> param to null
            CurrentOrganization::class -> {
                param to CurrentOrganization.of(environment.graphQlContext.authenticationDetails())
            }
            OAuthToken::class -> {
                val details = environment.graphQlContext.authenticationDetails()
                param to authenticator.getTokenOrRefresh(details.userId)
            }
            else -> null
        }
    }
}
