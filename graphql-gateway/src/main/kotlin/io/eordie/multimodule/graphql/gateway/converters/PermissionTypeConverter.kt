package io.eordie.multimodule.graphql.gateway.converters

import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLType
import java.security.Permission
import kotlin.reflect.KClass
import kotlin.reflect.KType

class PermissionTypeConverter : TypeConverter {
    override fun supports(): List<KClass<*>> = listOf(Permission::class)
    override fun typeName(projectionName: String?): String? = null

    override fun convert(type: KType, projectionName: String?): GraphQLType {
        GraphQLEnumType.newEnum()
            .name(Permission::class.simpleName)
            .build()

        return GraphQLObjectType.newObject()
            .build()
    }
}