package io.eordie.multimodule.example.gateway.config

import com.expediagroup.graphql.dataloader.KotlinDataLoader
import com.expediagroup.graphql.dataloader.KotlinDataLoaderRegistryFactory
import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.toSchema
import graphql.GraphQL
import io.eordie.multimodule.example.contracts.Mutation
import io.eordie.multimodule.example.contracts.Query
import io.eordie.multimodule.example.gateway.converters.TypeConverter
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import org.dataloader.DataLoaderRegistry
import kotlin.reflect.full.declaredFunctions

@Factory
class GraphQLFactory {

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
        queryServices: List<Query>,
        mutationServices: List<Mutation>,
        customConverters: List<TypeConverter>
    ): GraphQL {
        val config = SchemaGeneratorConfig(
            supportedPackages = listOf(
                "io.eordie.multimodule.example.contracts.models"
            ),
            hooks = CustomGeneratorHooks(customConverters)
        )

        val queries = queryServices.map { TopLevelObject(it) }
        val mutations = mutationServices.map { TopLevelObject(it) }
        val graphQLSchema = toSchema(config, queries, mutations)

        return GraphQL.newGraphQL(graphQLSchema)
            .build()
    }
}
