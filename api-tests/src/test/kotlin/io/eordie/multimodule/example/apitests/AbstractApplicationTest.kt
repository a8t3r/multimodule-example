package io.eordie.multimodule.example.apitests

import graphql.GraphQLContext
import graphql.execution.ExecutionId
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.eordie.multimodule.example.SyntheticSupport
import io.eordie.multimodule.example.apitests.AuthUtils.getAuthentication
import io.eordie.multimodule.example.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.example.gateway.graphql.ContextKeys
import io.eordie.multimodule.example.rsocket.context.getAuthenticationContext
import io.micronaut.context.ApplicationContext
import io.micronaut.context.DefaultApplicationContextBuilder
import io.micronaut.core.propagation.PropagatedContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerHttpRequestContext
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.dataloader.DataLoaderRegistry
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.coroutines.coroutineContext

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest(contextBuilder = [AbstractApplicationTest.CustomContextBuilder::class])
abstract class AbstractApplicationTest {

    companion object {
        val defaultOrganization = CurrentOrganization(UUID.fromString("70b3b9e8-1c63-4768-98f1-29ae087de907"))
    }

    @Inject
    lateinit var dataLoaderRegistry: (GraphQLContext) -> DataLoaderRegistry

    protected suspend fun env(): DataFetchingEnvironment {
        val context = coroutineContext
        val graphQLContext = GraphQLContext.of {
            it.of(ContextKeys.COROUTINE_CONTEXT, context)
            it.of(ContextKeys.AUTHENTICATION, context.getAuthenticationContext().getAuthentication())
        }

        return DataFetchingEnvironmentImpl.newDataFetchingEnvironment()
            .executionId(ExecutionId.from("synthetic"))
            .dataLoaderRegistry(dataLoaderRegistry(graphQLContext))
            .graphQLContext(graphQLContext)
            .build()
    }

    class CustomContextBuilder : DefaultApplicationContextBuilder() {
        override fun newApplicationContext(): ApplicationContext {
            // required for authentication mock
            PropagatedContext.empty()
                .plus(ServerHttpRequestContext(HttpRequest.GET<Any>("")))
                .propagate()

            return SyntheticSupport()
                .eagerInitSingletons(true)
                .eagerInitConfiguration(true)
                .build()
        }
    }
}
