package io.eordie.multimodule.graphql.gateway.converters

import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputObjectField
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLInputType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLOutputType
import graphql.schema.GraphQLType
import graphql.schema.GraphQLTypeReference
import io.eordie.multimodule.contracts.utils.getIntrospection
import io.eordie.multimodule.contracts.utils.uncheckedCast
import io.micronaut.core.beans.BeanProperty
import kotlin.reflect.KClass

class GenericTypeConverter(private val targetClass: KClass<*>) {
    fun convertToInput(projectionName: String): GraphQLInputType {
        val introspection = getIntrospection<Any>(targetClass)

        return GraphQLInputObjectType.newInputObject()
            .name("${projectionName}${targetClass.simpleName}")
            .apply {
                introspection.beanProperties.forEach { property ->
                    field(
                        GraphQLInputObjectField.newInputObjectField()
                            .name(property.name)
                            .type(typeOf<GraphQLInputType>(property, projectionName))
                            .build()
                    )
                }
            }
            .build()
    }

    fun convertToOutput(projectionName: String): GraphQLOutputType {
        val introspection = getIntrospection<Any>(targetClass)

        return GraphQLObjectType.newObject()
            .name("${projectionName}${targetClass.simpleName}")
            .apply {
                introspection.beanProperties.forEach { property ->
                    field(
                        GraphQLFieldDefinition.newFieldDefinition()
                            .name(property.name)
                            .type(typeOf<GraphQLObjectType>(property, projectionName))
                            .build()
                    )
                }
            }
            .build()
    }

    private inline fun <reified T : GraphQLType> typeOf(
        property: BeanProperty<Any, Any>,
        projectionName: String
    ): T = when (property.type) {
        Enum::class.java -> GraphQLTypeReference.typeRef(projectionName)
        List::class.java -> GraphQLList.list(GraphQLTypeReference.typeRef(projectionName))
        else -> GraphQLTypeReference.typeRef(property.type.simpleName)
    }.uncheckedCast()
}
