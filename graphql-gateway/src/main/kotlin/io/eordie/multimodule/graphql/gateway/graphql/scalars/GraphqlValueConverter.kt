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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object GraphqlValueConverter {
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
