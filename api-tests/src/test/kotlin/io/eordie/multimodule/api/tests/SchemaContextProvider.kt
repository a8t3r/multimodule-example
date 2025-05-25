package io.eordie.multimodule.api.tests

import io.eordie.multimodule.common.security.context.getAuthenticationContext
import io.eordie.multimodule.contracts.utils.JsonModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.micronaut.http.HttpHeaders
import io.micronaut.runtime.server.EmbeddedServer
import jakarta.inject.Singleton
import kobby.kotlin.SchemaContext
import kobby.kotlin.SchemaSubscriber
import kobby.kotlin.adapter.ktor.SchemaSimpleKtorAdapter
import kobby.kotlin.entity.Mutation
import kobby.kotlin.entity.MutationProjection
import kobby.kotlin.entity.Query
import kobby.kotlin.entity.QueryProjection
import kobby.kotlin.entity.Subscription
import kobby.kotlin.entity.SubscriptionProjection
import kobby.kotlin.schemaContextOf
import kobby.kotlin.schemaJson
import kotlinx.serialization.encodeToString
import kotlin.coroutines.coroutineContext

@Singleton
class SchemaContextProvider(private val server: EmbeddedServer) : SchemaContext {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(schemaJson)
        }
    }

    private val headers: suspend () -> Map<String, String> = {
        val auth = coroutineContext.getAuthenticationContext()
        mapOf(HttpHeaders.X_AUTH_TOKEN to JsonModule.getInstance().encodeToString(auth))
    }

    private val context by lazy {
        schemaContextOf(
            SchemaSimpleKtorAdapter(client, "http://localhost:${server.port}/graphql", headers)
        )
    }

    override suspend fun query(__projection: QueryProjection.() -> Unit): Query {
        return context.query(__projection)
    }

    override suspend fun mutation(__projection: MutationProjection.() -> Unit): Mutation {
        return context.mutation(__projection)
    }

    override fun subscription(__projection: SubscriptionProjection.() -> Unit): SchemaSubscriber<Subscription> {
        return context.subscription(__projection)
    }
}
