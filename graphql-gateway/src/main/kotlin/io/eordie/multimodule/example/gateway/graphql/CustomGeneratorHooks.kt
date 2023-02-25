package io.eordie.multimodule.example.gateway.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.expediagroup.graphql.generator.hooks.FlowSubscriptionSchemaGeneratorHooks
import graphql.Scalars
import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLList
import graphql.schema.GraphQLType
import graphql.schema.GraphQLTypeReference
import io.eordie.multimodule.example.gateway.converters.TypeConverter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.superclasses

inline fun <reified T : Annotation> KFunction<*>.findAnnotations(kClass: KClass<*>): List<T> {
    val contract = kClass.superclasses.first().declaredFunctions
        .firstOrNull { it.name == this.name } ?: return emptyList()

    return contract.findAnnotations(T::class)
}

internal class CustomGeneratorHooks(customConverters: List<TypeConverter>) : FlowSubscriptionSchemaGeneratorHooks() {

    private val converters = customConverters
        .flatMap { converter -> converter.supports().map { it to converter } }
        .toMap()

    private val previouslyCreated = mutableSetOf<String>()

    override fun isValidFunction(kClass: KClass<*>, function: KFunction<*>) =
        function.findAnnotations<GraphQLIgnore>(kClass).isEmpty() &&
            !function.name.startsWith("internal") && !function.name.startsWith("load")

    @Suppress("CyclomaticComplexMethod")
    override fun willGenerateGraphQLType(type: KType): GraphQLType? = when (type.classifier as? KClass<*>) {
        UUID::class -> ExtendedScalars.UUID
        BigDecimal::class -> ExtendedScalars.GraphQLBigDecimal
        Byte::class -> ExtendedScalars.GraphQLByte
        Char::class -> ExtendedScalars.GraphQLChar
        Short::class -> ExtendedScalars.GraphQLShort
        Long::class -> ExtendedScalars.GraphQLLong
        Currency::class -> Scalars.GraphQLString
        LocalDate::class -> ExtendedScalars.Date
        LocalTime::class -> ExtendedScalars.LocalTime
        OffsetDateTime::class -> ExtendedScalars.DateTime
        Set::class -> {
            val projection = getProjection(type)
            GraphQLList.list(GraphQLTypeReference(projection))
        }

        else -> {
            val converter = converters[type.classifier]
            if (converter == null) null else {
                val projection = getProjection(type)
                val typeName = converter.typeName(projection)
                if (typeName != null && !previouslyCreated.add(typeName)) GraphQLTypeReference(typeName) else {
                    converter.convert(type, projection)
                }
            }
        }
    }

    private fun getProjection(type: KType, index: Int = 0): String? =
        (type.arguments[index].type?.classifier as? KClass<*>)?.simpleName
}
