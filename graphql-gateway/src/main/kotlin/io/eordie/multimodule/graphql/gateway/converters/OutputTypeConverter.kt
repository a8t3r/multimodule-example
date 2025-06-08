package io.eordie.multimodule.graphql.gateway.converters

import graphql.schema.GraphQLType
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface OutputTypeConverter {
    fun supports(): List<KClass<*>>
    fun typeName(projectionName: String?): String?
    fun convert(type: KType, projectionName: String?): GraphQLType
}
