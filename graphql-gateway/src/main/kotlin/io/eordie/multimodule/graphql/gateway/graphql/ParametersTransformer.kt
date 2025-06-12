package io.eordie.multimodule.graphql.gateway.graphql

import graphql.schema.DataFetchingEnvironment
import io.eordie.multimodule.contracts.basic.RequestHeaders
import io.eordie.multimodule.contracts.basic.paging.SelectionSet
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.identitymanagement.models.OAuthToken
import io.eordie.multimodule.contracts.utils.JsonModule
import io.eordie.multimodule.graphql.gateway.graphql.scalars.GraphqlValueConverter
import io.eordie.multimodule.graphql.gateway.security.KeycloakRefreshAuthenticator
import io.micronaut.core.type.Headers
import io.micronaut.core.type.MutableHeaders
import io.micronaut.http.HttpHeaders
import jakarta.inject.Singleton
import kotlinx.serialization.serializer
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KParameter
import kotlin.reflect.KType

@Singleton
class ParametersTransformer(
    private val authenticator: KeycloakRefreshAuthenticator
) {

    private val proto = JsonModule.getInstance()

    fun convert(value: Any, type: KType): Any? {
        val jsonElement = GraphqlValueConverter.toJsonElement(value)
        return proto.decodeFromJsonElement(proto.serializersModule.serializer(type), jsonElement)
    }

    fun mapParameterToValue(param: KParameter, environment: DataFetchingEnvironment): Pair<KParameter, Any?>? {
        val classifier = param.type.classifier
        return when (classifier) {
            Headers::class, RequestHeaders::class -> {
                val headers = environment.graphQlContext.get<HttpHeaders>(ContextKeys.HEADERS)
                param to RequestHeaders(headers.asMap())
            }
            MutableHeaders::class -> { param to environment.graphQlContext.get(ContextKeys.RESPONSE_HEADERS) }
            DataFetchingEnvironment::class -> param to environment
            SelectionSet::class -> param to SelectionSetExtractor.from(environment)
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
