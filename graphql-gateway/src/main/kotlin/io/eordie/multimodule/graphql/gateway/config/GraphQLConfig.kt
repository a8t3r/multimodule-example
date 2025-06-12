package io.eordie.multimodule.graphql.gateway.config

import com.expediagroup.graphql.apq.cache.DefaultAutomaticPersistedQueriesCache
import com.expediagroup.graphql.apq.provider.AutomaticPersistedQueriesProvider
import com.expediagroup.graphql.dataloader.KotlinDataLoader
import com.expediagroup.graphql.dataloader.KotlinDataLoaderRegistryFactory
import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.execution.FlowSubscriptionExecutionStrategy
import com.expediagroup.graphql.generator.execution.SimpleKotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.toSchema
import graphql.GraphQL
import graphql.GraphQLContext
import graphql.analysis.MaxQueryComplexityInstrumentation
import graphql.analysis.MaxQueryDepthInstrumentation
import graphql.execution.instrumentation.ChainedInstrumentation
import graphql.execution.preparsed.PreparsedDocumentProvider
import graphql.schema.DataFetcherFactory
import io.eordie.multimodule.common.rsocket.client.getServiceInterface
import io.eordie.multimodule.common.rsocket.client.route.Synthesized
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.Subscription
import io.eordie.multimodule.graphql.gateway.graphql.CacheProvider
import io.eordie.multimodule.graphql.gateway.graphql.CustomGeneratorHooks
import io.eordie.multimodule.graphql.gateway.graphql.DataFetcherExceptionHandler
import io.eordie.multimodule.graphql.gateway.graphql.FunctionKotlinDataLoader
import io.eordie.multimodule.graphql.gateway.graphql.GraphqlContextBuilder
import io.eordie.multimodule.graphql.gateway.graphql.ParametersTransformer
import io.eordie.multimodule.graphql.gateway.graphql.SecurityGraphQLExecutionInputCustomizer
import io.eordie.multimodule.graphql.gateway.graphql.TracingFunctionDataFetcher
import io.eordie.multimodule.graphql.gateway.graphql.instrumentation.FlowAsPublisherInstrumentation
import io.eordie.multimodule.graphql.gateway.graphql.instrumentation.OpenTelemetryTracingInstrumentation
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Prototype
import io.opentelemetry.api.OpenTelemetry
import jakarta.inject.Singleton
import org.dataloader.DataLoaderRegistry
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions

@Factory
class GraphQLConfig {

    @Bean
    fun executionInputCustomizer() = SecurityGraphQLExecutionInputCustomizer()

    @Bean
    fun dataloaderRegistryGenerator(factory: KotlinDataLoaderRegistryFactory): (GraphQLContext) -> DataLoaderRegistry {
        return {
            factory.generate(it)
        }
    }

    @Prototype
    fun dataLoaderRegistry(
        factory: KotlinDataLoaderRegistryFactory,
        contextBuilder: GraphqlContextBuilder
    ): DataLoaderRegistry {
        val context = contextBuilder.buildContext()
        return factory.generate(context)
    }

    @Singleton
    fun dataLoaderRegistryFactory(
        queries: List<Query>,
        dataLoaders: List<KotlinDataLoader<*, *>>,
        cacheProvider: CacheProvider
    ): KotlinDataLoaderRegistryFactory {
        val generated = filterOperations(queries, Query::class)
            .flatMap { (type, instance) ->
                type.declaredFunctions.map {
                    FunctionKotlinDataLoader(instance, it, cacheProvider)
                }
            }

        return KotlinDataLoaderRegistryFactory(dataLoaders + generated)
    }

    @Bean
    fun instrumentation(openTelemetry: OpenTelemetry, properties: GraphqlProperties): ChainedInstrumentation {
        val tracer = openTelemetry.tracerBuilder("graphql").build()
        return ChainedInstrumentation(
            FlowAsPublisherInstrumentation(exceptionHandler()),
            OpenTelemetryTracingInstrumentation(tracer),
            MaxQueryDepthInstrumentation(properties.maxDepth),
            MaxQueryComplexityInstrumentation(properties.maxComplexity)
        )
    }

    @Bean
    fun schemaGeneratorConfig(
        parametersTransformer: ParametersTransformer
    ): SchemaGeneratorConfig {
        return SchemaGeneratorConfig(
            supportedPackages = listOf(Query::class.java.packageName),
            hooks = CustomGeneratorHooks(),
            dataFetcherFactoryProvider = object : SimpleKotlinDataFetcherFactoryProvider() {
                override fun functionDataFetcherFactory(
                    target: Any?,
                    kClass: KClass<*>,
                    kFunction: KFunction<*>
                ): DataFetcherFactory<Any?> =
                    DataFetcherFactory { _ ->
                        TracingFunctionDataFetcher(
                            target,
                            kFunction,
                            parametersTransformer
                        )
                    }
            }
        )
    }

    // replace service type with super interface declared in model-contracts
    private fun <T : Any> filterOperations(operations: List<T>, type: KClass<T>): List<Pair<KClass<*>, T>> {
        return operations.groupBy { it::class.getServiceInterface(type) ?: it::class }
            .map { (targetClass, group) ->
                val instance = group.firstOrNull { it is Synthesized }
                    ?: error("synthesized implementation not found for type $targetClass")
                targetClass to instance
            }
    }

    @Bean
    fun preparsedDocumentProvider(): PreparsedDocumentProvider {
        return AutomaticPersistedQueriesProvider(DefaultAutomaticPersistedQueriesCache())
    }

    @Bean
    fun exceptionHandler() = DataFetcherExceptionHandler()

    @Bean
    fun graphqlSchema(
        instrumentation: ChainedInstrumentation,
        config: SchemaGeneratorConfig,
        queries: List<Query>,
        mutations: List<Mutation>,
        subscriptions: List<Subscription>
    ): GraphQL {
        fun <T : Any> topObjects(operations: List<T>, type: KClass<T>) = filterOperations(operations, type)
            .map { (type, instance) -> TopLevelObject(instance, type) }

        val graphQLSchema = toSchema(
            config,
            topObjects(queries, Query::class),
            topObjects(mutations, Mutation::class),
            topObjects(subscriptions, Subscription::class),
        )

        return GraphQL.newGraphQL(graphQLSchema)
            .subscriptionExecutionStrategy(FlowSubscriptionExecutionStrategy(exceptionHandler()))
            .preparsedDocumentProvider(preparsedDocumentProvider())
            .defaultDataFetcherExceptionHandler(exceptionHandler())
            .instrumentation(instrumentation)
            .build()
    }
}
