package io.eordie.multimodule.graphql.gateway.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.expediagroup.graphql.generator.hooks.FlowSubscriptionSchemaGeneratorHooks
import graphql.Scalars
import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLList
import graphql.schema.GraphQLType
import graphql.schema.GraphQLTypeReference
import io.eordie.multimodule.contracts.basic.geometry.TMultiPolygon
import io.eordie.multimodule.contracts.basic.geometry.TPoint
import io.eordie.multimodule.contracts.basic.geometry.TPolygon
import io.eordie.multimodule.graphql.gateway.converters.TypeConverter
import io.eordie.multimodule.graphql.gateway.graphql.scalars.TMultiPolygonScalarCoercing
import io.eordie.multimodule.graphql.gateway.graphql.scalars.TPointScalarCoercing
import io.eordie.multimodule.graphql.gateway.graphql.scalars.TPolygonScalarCoercing
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
    private val internalPrefixes = setOf("internal", "load", "broadcast")

    override fun isValidFunction(kClass: KClass<*>, function: KFunction<*>) =
        function.findAnnotations<GraphQLIgnore>(kClass).isEmpty() &&
            internalPrefixes.none { function.name.startsWith(it) }

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
        TPoint::class -> TPointScalarCoercing.Scalar
        TPolygon::class -> TPolygonScalarCoercing.Scalar
        TMultiPolygon::class -> TMultiPolygonScalarCoercing.Scalar
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
