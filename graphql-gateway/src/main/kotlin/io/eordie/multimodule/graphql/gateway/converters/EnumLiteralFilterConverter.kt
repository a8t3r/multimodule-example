package io.eordie.multimodule.graphql.gateway.converters

import graphql.Scalars
import graphql.schema.GraphQLInputObjectField
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLType
import graphql.schema.GraphQLTypeReference
import io.eordie.multimodule.contracts.basic.filters.EnumLiteralFilter
import io.eordie.multimodule.contracts.utils.getIntrospection
import jakarta.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.KType

@Singleton
class EnumLiteralFilterConverter : OutputTypeConverter {
    override fun supports(): List<KClass<*>> = listOf(EnumLiteralFilter::class)
    override fun typeName(projectionName: String?): String = "${projectionName}${EnumLiteralFilter::class.simpleName}"

    private val introspection = getIntrospection<EnumLiteralFilter<*>>(EnumLiteralFilter::class)

    override fun convert(type: KType, projectionName: String?): GraphQLType {
        return GraphQLInputObjectType.newInputObject()
            .name(typeName(projectionName))
            .apply {
                introspection.beanProperties.forEach { property ->
                    val targetType = when (property.type) {
                        Boolean::class.java -> Scalars.GraphQLBoolean
                        List::class.java -> GraphQLList.list(GraphQLTypeReference.typeRef(projectionName))
                        else -> GraphQLTypeReference.typeRef(projectionName)
                    }
                    field(
                        GraphQLInputObjectField.newInputObjectField()
                            .name(property.name)
                            .type(targetType)
                            .build()
                    )
                }
            }
            .build()
    }
}
