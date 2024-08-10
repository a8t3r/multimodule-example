package io.eordie.multimodule.common.repository

import io.eordie.multimodule.contracts.utils.getIntrospection
import io.micronaut.core.convert.ConversionService
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.ast.impl.TupleImplementor
import java.util.*
import kotlin.reflect.KClass

@Singleton
class TupleConverter(private val conversionService: ConversionService) {
    fun <T : Any> convert(tuple: TupleImplementor, target: KClass<T>): T {
        val introspection = getIntrospection<T>(target)
        val arguments = introspection.constructorArguments
        if (tuple.size() != arguments.size) error("size mismatch")

        val parameters = arguments.mapIndexed { index, arg ->
            val value = tuple.get(index) ?: Optional.empty<Any>()
            val result = conversionService.convert(value, arg)
            result.orElseGet {
                if (arg.isNullable) null else {
                    error("missing argument")
                }
            }
        }

        return introspection.constructor.instantiate(*parameters.toTypedArray())
    }
}
