package io.eordie.multimodule.example.gateway.converters

import graphql.schema.GraphQLNamedType
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface TypeConverter {
    fun supports(): List<KClass<*>>
    fun typeName(projectionName: String?): String
    fun convert(type: KType, projectionName: String?): GraphQLNamedType
}
