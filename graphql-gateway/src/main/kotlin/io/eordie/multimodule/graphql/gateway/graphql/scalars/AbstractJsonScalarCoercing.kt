package io.eordie.multimodule.graphql.gateway.graphql.scalars

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingSerializeException
import io.eordie.multimodule.contracts.utils.JsonModule
import io.eordie.multimodule.contracts.utils.safeCast
import io.eordie.multimodule.contracts.utils.uncheckedCast
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import java.util.*
import kotlin.reflect.KClass

abstract class AbstractJsonScalarCoercing<In : Any, Out : Any>(
    private val inputType: KClass<In>,
    outputType: KClass<Out>
) : Coercing<In, Out> {

    companion object {
        private val json = JsonModule.getInstance()
    }

    private val outSerializer: KSerializer<Out> = safeCast(json.serializersModule.serializer(outputType.javaObjectType))

    abstract fun fromInput(input: In): Out
    abstract fun toInput(output: Out): In

    override fun parseValue(input: Any, graphQLContext: GraphQLContext, locale: Locale): In? {
        val element = GraphqlValueConverter.toJsonElement(input)
        return parseJsonElement(element)
    }

    override fun serialize(input: Any, graphQLContext: GraphQLContext, locale: Locale): Out? {
        if (!inputType.isInstance(input)) {
            throw CoercingSerializeException("Invalid input class ${input.javaClass.name}")
        }

        return try {
            fromInput(input.uncheckedCast())
        } catch (e: Exception) {
            throw CoercingSerializeException(e.message, e)
        }
    }

    override fun parseLiteral(
        input: Value<*>,
        variables: CoercedVariables,
        graphQLContext: GraphQLContext,
        locale: Locale
    ): In? {
        val tree = GraphqlValueConverter.convert(input)
        return parseJsonElement(tree)
    }

    private fun parseJsonElement(jsTree: JsonElement): In {
        try {
            val out = json.decodeFromJsonElement(outSerializer, jsTree)
            return toInput(out)
        } catch (e: CoercingParseLiteralException) {
            throw e
        } catch (e: Throwable) {
            throw CoercingParseLiteralException(e.message, e)
        }
    }
}
