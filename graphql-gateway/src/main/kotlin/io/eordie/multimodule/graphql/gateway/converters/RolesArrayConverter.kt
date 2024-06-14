package io.eordie.multimodule.graphql.gateway.converters

import graphql.schema.GraphQLList
import graphql.schema.GraphQLType
import graphql.schema.GraphQLTypeReference
import io.eordie.multimodule.contracts.utils.Roles
import jakarta.inject.Singleton
import kotlin.reflect.KType

@Singleton
class RolesArrayConverter : TypeConverter {
    override fun supports() = listOf(Array<Roles>::class)
    override fun typeName(projectionName: String?) = null
    override fun convert(type: KType, projectionName: String?): GraphQLType {
        return GraphQLList.list(GraphQLTypeReference(projectionName))
    }
}
