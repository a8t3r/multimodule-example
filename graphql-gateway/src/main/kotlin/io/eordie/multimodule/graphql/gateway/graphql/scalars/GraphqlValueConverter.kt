package io.eordie.multimodule.graphql.gateway.graphql.scalars

import graphql.language.ArrayValue
import graphql.language.BooleanValue
import graphql.language.EnumValue
import graphql.language.FloatValue
import graphql.language.IntValue
import graphql.language.NullValue
import graphql.language.ObjectValue
import graphql.language.StringValue
import graphql.language.Value
import io.eordie.multimodule.contracts.basic.geometry.SpatialReference
import io.eordie.multimodule.contracts.utils.JsonModule
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement

object GraphqlValueConverter {

    fun toJsonElement(value: Any?): JsonElement = when (value) {
        null -> JsonNull
        is JsonElement -> value
        is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is String -> JsonPrimitive(value)
        is Array<*> -> JsonArray(value.map { toJsonElement(it) })
        is List<*> -> JsonArray(value.map { toJsonElement(it) })
        is Map<*, *> -> JsonObject(value.map { it.key.toString() to toJsonElement(it.value) }.toMap())
        is SpatialReference -> JsonModule.getInstance().encodeToJsonElement<SpatialReference>(value)
        else -> error("too complex type")
    }

    fun convert(input: Value<*>): JsonElement {
        return when (input) {
            is NullValue -> JsonNull
            is BooleanValue -> JsonPrimitive(input.isValue)
            is IntValue -> JsonPrimitive(input.value)
            is StringValue -> JsonPrimitive(input.value)
            is FloatValue -> JsonPrimitive(input.value)
            is EnumValue -> JsonPrimitive(input.name)
            is ArrayValue -> JsonArray(input.values.map { convert(it) })
            is ObjectValue -> JsonObject(input.objectFields.associateBy({ it.name }, { convert(it.value) }))
            else -> error("unknown type ${input::class}")
        }
    }
}
