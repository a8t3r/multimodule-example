package io.eordie.multimodule.example.gateway.config

import com.expediagroup.graphql.dataloader.KotlinDataLoader
import com.expediagroup.graphql.dataloader.KotlinDataLoaderRegistryFactory
import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.execution.SimpleKotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.toSchema
import graphql.GraphQL
import graphql.GraphQLContext
import graphql.analysis.MaxQueryComplexityInstrumentation
import graphql.analysis.MaxQueryDepthInstrumentation
import graphql.execution.instrumentation.ChainedInstrumentation
import graphql.schema.DataFetcherFactory
import io.eordie.multimodule.example.contracts.Mutation
import io.eordie.multimodule.example.contracts.Query
import io.eordie.multimodule.example.gateway.converters.TypeConverter
import io.eordie.multimodule.example.gateway.graphql.CustomGeneratorHooks
import io.eordie.multimodule.example.gateway.graphql.DataFetcherExceptionHandler
import io.eordie.multimodule.example.gateway.graphql.FunctionKotlinDataLoader
import io.eordie.multimodule.example.gateway.graphql.GraphqlContextBuilder
import io.eordie.multimodule.example.gateway.graphql.OpenTelemetryTracingInstrumentation
import io.eordie.multimodule.example.gateway.graphql.ParametersTransformer
import io.eordie.multimodule.example.gateway.graphql.SecurityGraphQLExecutionInputCustomizer
import io.eordie.multimodule.example.gateway.graphql.TracingFunctionDataFetcher
import io.eordie.multimodule.example.rsocket.client.getServiceInterface
import io.eordie.multimodule.example.rsocket.client.invocation.Synthesized
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
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

    @Bean
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
        dataLoaders: List<KotlinDataLoader<*, *>>
    ): KotlinDataLoaderRegistryFactory {
        val generated = filterOperations(queries)
            .flatMap { (type, instance) ->
                type.declaredFunctions.map {
                    FunctionKotlinDataLoader(instance, it)
                }
            }

        return KotlinDataLoaderRegistryFactory(dataLoaders + generated)
    }

    @Bean
    fun instrumentation(openTelemetry: OpenTelemetry, properties: GraphqlProperties): ChainedInstrumentation {
        val tracer = openTelemetry.tracerBuilder("graphql").build()
        return ChainedInstrumentation(
            OpenTelemetryTracingInstrumentation(tracer),
            MaxQueryDepthInstrumentation(properties.maxDepth),
            MaxQueryComplexityInstrumentation(properties.maxComplexity)
        )
    }

    @Bean
    fun schemaGeneratorConfig(
        customConverters: List<TypeConverter>,
        parametersTransformer: ParametersTransformer
    ): SchemaGeneratorConfig {
        return SchemaGeneratorConfig(
            supportedPackages = listOf(Query::class.java.packageName),
            hooks = CustomGeneratorHooks(customConverters),
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
    private fun <T : Any> filterOperations(operations: List<T>): List<Pair<KClass<*>, T>> {
        return operations.groupBy { it::class.getServiceInterface() ?: it::class }
            .map { (targetClass, group) ->
                val instance = group.firstOrNull { it is Synthesized }
                    ?: error("synthesized implementation not found for type $targetClass")
                targetClass to instance
            }
    }

    @Bean
    @Requires(classes = [TypeConverter::class])
    fun graphqlSchema(
        instrumentation: ChainedInstrumentation,
        config: SchemaGeneratorConfig,
        queries: List<Query>,
        mutations: List<Mutation>
    ): GraphQL {
        fun topObjects(operations: List<Any>) = filterOperations(operations)
            .map { (type, instance) -> TopLevelObject(instance, type) }

        val graphQLSchema = toSchema(
            config,
            topObjects(queries),
            topObjects(mutations)
        )
        return GraphQL.newGraphQL(graphQLSchema)
            .defaultDataFetcherExceptionHandler(DataFetcherExceptionHandler())
            .instrumentation(instrumentation)
            .build()
    }
}
