package io.eordie.multimodule.graphql.gateway.graphql.scalars

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingSerializeException
import io.eordie.multimodule.contracts.utils.JsonModule
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import java.util.*
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
abstract class AbstractJsonScalarCoercing<In : Any, Out : Any>(
    private val inputType: KClass<In>,
    outputType: KClass<Out>
) : Coercing<In, Out> {

    companion object {
        private val json = JsonModule.getInstance()
    }

    private val outSerializer = json.serializersModule.serializer(outputType.javaObjectType) as KSerializer<Out>

    abstract fun fromInput(input: In): Out
    abstract fun toInput(output: Out): In

    override fun serialize(input: Any, graphQLContext: GraphQLContext, locale: Locale): Out? {
        if (!inputType.isInstance(input)) {
            throw CoercingSerializeException("Invalid input class ${input.javaClass.name}")
        }

        return try {
            fromInput(input as In)
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
        val jsTree = GraphqlValueConverter.convert(input)

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
