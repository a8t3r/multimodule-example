package io.eordie.multimodule.graphql.gateway.converters

import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLList
import graphql.schema.GraphQLNamedType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLTypeReference
import io.eordie.multimodule.contracts.basic.paging.Page
import io.eordie.multimodule.contracts.basic.paging.Pageable
import jakarta.inject.Singleton
import kotlin.reflect.KType

@Singleton
class PageConverter : OutputTypeConverter {
    override fun supports() = listOf(Page::class)
    override fun typeName(projectionName: String?) = "${projectionName}Page"

    override fun convert(type: KType, projectionName: String?): GraphQLNamedType {
        return GraphQLObjectType.newObject()
            .name(typeName(projectionName))
            .field(
                GraphQLFieldDefinition.newFieldDefinition()
                    .name(Page<*>::data.name)
                    .type(GraphQLList.list(GraphQLTypeReference(projectionName)))
            )
            .field(
                GraphQLFieldDefinition.newFieldDefinition()
                    .name(Page<*>::pageable.name)
                    .type(GraphQLTypeReference(Pageable::class.simpleName))
            )
            .build()
    }
}
