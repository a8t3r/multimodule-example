package io.eordie.multimodule.api.tests

import graphql.GraphQLContext
import graphql.execution.ExecutionId
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import io.eordie.multimodule.common.SyntheticSupport
import io.eordie.multimodule.common.security.context.AuthenticationContextElement
import io.eordie.multimodule.common.security.context.getAuthenticationContext
import io.eordie.multimodule.contracts.identitymanagement.models.CurrentOrganization
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.graphql.gateway.graphql.ContextKeys
import io.micronaut.context.ApplicationContext
import io.micronaut.context.DefaultApplicationContextBuilder
import io.micronaut.core.propagation.PropagatedContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerHttpRequestContext
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.dataloader.DataLoaderRegistry
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest(contextBuilder = [AbstractApplicationTest.CustomContextBuilder::class])
abstract class AbstractApplicationTest {

    companion object {
        val developer1: UUID = UUID.fromString("bd918ec6-7b7d-49a6-93fa-824191ba261f")
        val developer2: UUID = UUID.fromString("fb51139c-2348-417a-b486-ae5bba6a34cf")
        val developersOrg = CurrentOrganization(UUID.fromString("70b3b9e8-1c63-4768-98f1-29ae087de907"))
        val firstOrg = CurrentOrganization(UUID.fromString("c55e2b54-a0f9-425b-bd2e-e64f9e441eb8"))
        val secondOrg = CurrentOrganization(UUID.fromString("d11e0aee-be2c-413a-9001-0856430a8d71"))

        val authorization = AuthUtils.authWith(developersOrg)
    }

    fun test(
        auth: CoroutineContext = authorization,
        block: suspend TestScope.() -> Unit
    ) = runTest(auth, Duration.parse("30s"), block)

    operator fun AuthenticationContextElement.minus(role: Roles): AuthenticationContextElement =
        AuthenticationContextElement(details.copy(roleSet = details.roleSet.apply { remove(role) }))

    operator fun AuthenticationContextElement.plus(role: Roles): AuthenticationContextElement =
        AuthenticationContextElement(details.copy(roleSet = details.roleSet.apply { add(role) }))

    @Inject
    lateinit var schema: SchemaContextProvider

    @Inject
    lateinit var dataLoaderRegistry: (GraphQLContext) -> DataLoaderRegistry

    protected suspend fun env(): DataFetchingEnvironment {
        val context = coroutineContext
        val graphQLContext = GraphQLContext.of {
            it.of(ContextKeys.COROUTINE_CONTEXT, context)
            it.of(ContextKeys.AUTHENTICATION_DETAILS, context.getAuthenticationContext())
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
