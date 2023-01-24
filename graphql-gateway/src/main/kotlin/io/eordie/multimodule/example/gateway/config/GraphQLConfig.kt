package io.eordie.multimodule.example.gateway.config

import com.expediagroup.graphql.dataloader.KotlinDataLoader
import com.expediagroup.graphql.dataloader.KotlinDataLoaderRegistryFactory
import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.execution.SimpleKotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.toSchema
import graphql.GraphQL
import graphql.analysis.MaxQueryComplexityInstrumentation
import graphql.analysis.MaxQueryDepthInstrumentation
import graphql.execution.instrumentation.ChainedInstrumentation
import graphql.schema.DataFetcherFactory
import io.eordie.multimodule.example.contracts.utils.Mutation
import io.eordie.multimodule.example.contracts.utils.Query
import io.eordie.multimodule.example.gateway.converters.TypeConverter
import io.eordie.multimodule.example.gateway.graphql.CustomGeneratorHooks
import io.eordie.multimodule.example.gateway.graphql.FunctionKotlinDataLoader
import io.eordie.multimodule.example.gateway.graphql.SecuredFunctionDataFetcher
import io.eordie.multimodule.example.gateway.graphql.SecurityGraphQLExecutionInputCustomizer
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import org.dataloader.DataLoaderRegistry
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions

@Factory
class GraphQLConfig {

    @Bean
    fun executionInputCustomizer() = SecurityGraphQLExecutionInputCustomizer()

    @Bean
    fun dataLoaderRegistry(
        queryServices: List<Query>,
        dataLoaders: List<KotlinDataLoader<*, *>>
    ): DataLoaderRegistry {
        val generated = queryServices
            .flatMap { instance -> instance::class.declaredFunctions.map { it to instance } }
            .map { (function, instance) -> FunctionKotlinDataLoader(instance, function) }

        return KotlinDataLoaderRegistryFactory(dataLoaders + generated).generate()
    }

    @Bean
    @Requires(classes = [TypeConverter::class])
    fun graphqlSchema(
        properties: GraphqlProperties,
        queryServices: List<Query>,
        mutationServices: List<Mutation>,
        customConverters: List<TypeConverter>
    ): GraphQL {
        val config = SchemaGeneratorConfig(
            supportedPackages = listOf(Query::class.java.packageName),
            hooks = CustomGeneratorHooks(customConverters),
            dataFetcherFactoryProvider = object : SimpleKotlinDataFetcherFactoryProvider() {
                override fun functionDataFetcherFactory(target: Any?, kFunction: KFunction<*>) = DataFetcherFactory {
                    SecuredFunctionDataFetcher(
                        target = target,
                        fn = kFunction
                    )
                }
            }
        )

        val queries = queryServices.map { TopLevelObject(it) }
        val mutations = mutationServices.map { TopLevelObject(it) }
        val graphQLSchema = toSchema(config, queries, mutations)

        return GraphQL.newGraphQL(graphQLSchema)
            .instrumentation(
                ChainedInstrumentation(
                    MaxQueryDepthInstrumentation(properties.maxDepth),
                    MaxQueryComplexityInstrumentation(properties.maxComplexity)
                )
            )
            .build()
    }
}
