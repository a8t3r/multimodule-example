package io.eordie.multimodule.common.repository

import io.eordie.multimodule.contracts.basic.paging.Pageable
import io.eordie.multimodule.contracts.basic.paging.SortDirection
import io.eordie.multimodule.contracts.basic.paging.SortOrder
import io.eordie.multimodule.contracts.utils.ProtobufModule
import io.micronaut.core.convert.ConversionService
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.babyfish.jimmer.sql.ast.impl.ExpressionImplementor
import org.babyfish.jimmer.sql.ast.query.Order
import org.babyfish.jimmer.sql.ast.tuple.Tuple6
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.and
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.gt
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.babyfish.jimmer.sql.kt.ast.expression.lt
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.babyfish.jimmer.sql.kt.ast.table.makeOrders
import java.util.*

internal class InternalCursor(
    private val orderBy: List<SortOrder>,
    private val properties: List<PropertySpec>,
    private val sortingExpressionKeys: Set<String>
) {

    @Serializable
    data class PropertySpec(
        val order: SortOrder,
        val value: String?
    )

    companion object {

        private val proto = ProtobufModule.getInstance()

        fun create(
            pageable: Pageable,
            idPropertyName: String,
            sortingExpressionKeys: Set<String>
        ): InternalCursor {
            val cursor = pageable.cursor
            return if (cursor == null) {
                val orderBy = pageable.orderBy.orEmpty() + SortOrder(idPropertyName, SortDirection.ASC)
                InternalCursor(orderBy, emptyList(), sortingExpressionKeys)
            } else {
                val specs = proto.decodeFromByteArray<List<PropertySpec>>(Base64.getUrlDecoder().decode(cursor))
                InternalCursor(specs.map { it.order }, specs, sortingExpressionKeys)
            }
        }
    }

    private data class PropertyExpression(
        val propertySpec: PropertySpec,
        val value: Optional<out Any>,
        val expr: KPropExpression<out Comparable<*>>
    )

    fun getOrderBy(table: KNonNullTable<*>): List<Order> {
        return orderBy.filter { it.property != null }
            .map { "${it.property} ${(it.direction ?: SortDirection.ASC).name.lowercase()}" }
            .let { table.makeOrders(*it.toTypedArray()) }
    }

    fun next(
        conversionService: ConversionService,
        lastElement: Tuple6<*, out Any, out Any, out Any, out Any, out Any>?
    ): InternalCursor {
        val nextProperties = lastElement?.let { element ->
            orderBy.mapIndexed { index, order ->
                val lastValue = element.get(index + 1)
                val value = conversionService.convert(lastValue, String::class.java).orElseThrow()
                PropertySpec(order, value)
            }
        }.orEmpty()

        return InternalCursor(this.orderBy, nextProperties, sortingExpressionKeys)
    }

    fun asPageable(): Pageable {
        val nextCursor = if (properties.isEmpty()) null else {
            Base64.getEncoder().encodeToString(proto.encodeToByteArray(properties))
        }
        return Pageable(nextCursor, supportedOrders = sortingExpressionKeys)
    }

    /**
     * (a, b, c, d) ->
     *      (a > ?) or
     *      (a = ? and b > ?) or
     *      (a = ? and b = ? and c > ?) or
     *      (a = ? and b = ? and c = ? and d > ?)
     */
    private class Accumulator(
        val predicates: MutableList<KNonNullExpression<Boolean>> = mutableListOf(),
        var equalityExpression: KNonNullExpression<Boolean>? = null
    ) {
        fun accept(difference: KNonNullExpression<Boolean>, equality: KNonNullExpression<Boolean>): Accumulator {
            val predicate = and(equalityExpression, difference)
            if (predicate != null) {
                predicates.add(predicate)
            }
            equalityExpression = and(equalityExpression, equality)
            return this
        }
    }

    fun toPredicates(
        conversionService: ConversionService,
        sortingExpressions: Map<String, KPropExpression<out Comparable<*>>>
    ): List<KNonNullExpression<Boolean>> {
        return buildPropertyExpressions(conversionService, sortingExpressions)
            .map { transformAsPredicates(it) }
            .fold(Accumulator()) { acc, (difference, equality) ->
                acc.accept(difference, equality)
            }.predicates
    }

    private fun transformAsPredicates(
        propertyExpression: PropertyExpression
    ): Pair<KNonNullExpression<Boolean>, KNonNullExpression<Boolean>> {
        val expr = propertyExpression.expr as KPropExpression<Comparable<*>>
        return if (propertyExpression.value.isEmpty) expr.isNull() to expr.isNull() else {
            val value = propertyExpression.value.get() as Comparable<*>
            val predicate = when (propertyExpression.propertySpec.order.direction) {
                SortDirection.DESC -> expr.lt(value)
                else -> expr.gt(value)
            }

            predicate to expr.eq(value)
        }
    }

    private fun buildPropertyExpressions(
        conversionService: ConversionService,
        sortingExpressions: Map<String, KPropExpression<out Comparable<*>>>
    ): List<PropertyExpression> {
        return properties.map { spec ->
            val expression = requireNotNull(sortingExpressions[spec.order.property])
            val targetType = (expression as ExpressionImplementor<*>).type
            val value = conversionService.convert(spec.value, targetType)
            PropertyExpression(spec, value, expression)
        }
    }
}
