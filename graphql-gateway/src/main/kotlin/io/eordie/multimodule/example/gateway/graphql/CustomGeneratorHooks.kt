package io.eordie.multimodule.example.gateway.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import graphql.Scalars
import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLType
import graphql.schema.GraphQLTypeReference
import io.eordie.multimodule.example.gateway.converters.TypeConverter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
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

internal class CustomGeneratorHooks(customConverters: List<TypeConverter>) : SchemaGeneratorHooks {

    private val converters = customConverters
        .flatMap { converter -> converter.supports().map { it to converter } }
        .toMap()

    private val previouslyCreated = mutableSetOf<String>()

    override fun isValidFunction(kClass: KClass<*>, function: KFunction<*>) =
        function.findAnnotations<GraphQLIgnore>(kClass).isEmpty()

    override fun willGenerateGraphQLType(type: KType): GraphQLType? = when (type.classifier as? KClass<*>) {
        UUID::class -> ExtendedScalars.UUID
        BigDecimal::class -> ExtendedScalars.GraphQLBigDecimal
        Currency::class -> Scalars.GraphQLString
        LocalDate::class -> ExtendedScalars.Date
        LocalTime::class -> ExtendedScalars.LocalTime
        OffsetDateTime::class -> ExtendedScalars.DateTime
        ZonedDateTime::class -> ExtendedScalars.DateTime

        else -> {
            val converter = converters[type.classifier]
            if (converter == null) null else {
                val projection = getProjection(type)
                val typeName = converter.typeName(projection)
                if (!previouslyCreated.add(typeName)) GraphQLTypeReference(typeName) else {
                    converter.convert(type, projection)
                }
            }
        }
    }

    private fun getProjection(type: KType, index: Int = 0): String? =
        (type.arguments[index].type?.classifier as? KClass<*>)?.simpleName
}
