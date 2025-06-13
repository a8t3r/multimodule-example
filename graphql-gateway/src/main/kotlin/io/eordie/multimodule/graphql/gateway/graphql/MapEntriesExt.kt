package io.eordie.multimodule.graphql.gateway.graphql

import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputObjectField
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLTypeReference
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

internal fun newTypeKV(type: KType, target: KClass<*>): KType {
    val mapEntryArguments = requireNotNull(type.arguments[0].type?.arguments)
    return List::class.createType(
        listOf(
            KTypeProjection.invariant(
                target.createType(
                    listOf(
                        KTypeProjection.invariant(mapEntryArguments[0].type!!),
                        KTypeProjection.invariant(mapEntryArguments[1].type!!)
                    )
                )
            )
        )
    )
}

internal data class InputMapEntry<K, V>(val key: K, val value: V)
internal data class OutputMapEntry<K, V>(val key: K, val value: V)

internal fun mapInputType(k: String, v: String) = GraphQLInputObjectType.newInputObject()
    .name("InputMapEntry${k}To$v")
    .field(
        GraphQLInputObjectField.newInputObjectField()
            .name("key")
            .type(GraphQLTypeReference(k))
            .build()
    )
    .field(
        GraphQLInputObjectField.newInputObjectField()
            .name("value")
            .type(GraphQLTypeReference(v))
            .build()
    )
    .build()

internal fun mapOutputType(k: String, v: String) = GraphQLObjectType.newObject()
    .name("OutputMapEntry${k}To$v")
    .field(
        GraphQLFieldDefinition.newFieldDefinition()
            .name("key")
            .type(GraphQLTypeReference(k))
            .build()
    )
    .field(
        GraphQLFieldDefinition.newFieldDefinition()
            .name("value")
            .type(GraphQLTypeReference(v))
            .build()
    )
    .build()
