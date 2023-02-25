package io.eordie.multimodule.example.rsocket.client.invocation

import io.eordie.multimodule.example.contracts.annotations.Group
import io.eordie.multimodule.example.contracts.annotations.Valid
import io.eordie.multimodule.example.contracts.basic.exception.ValidationError
import io.eordie.multimodule.example.contracts.basic.exception.ValidationException
import io.eordie.multimodule.example.validation.EntityValidator
import io.micronaut.context.BeanLocator
import io.micronaut.core.type.Argument
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.javaType

object ValidationCheck {
    @OptIn(ExperimentalStdlibApi::class)
    suspend fun check(beanLocator: BeanLocator, arguments: List<Pair<KParameter, Any>>): ValidationException? {
        val errors = arguments.flatMap { (parameter, value) ->
            val validator = beanLocator.findBean(
                Argument.of(EntityValidator::class.java, Argument.of(parameter.type.javaType))
            ).orElseThrow {
                error("no validator found for type ${parameter.type}")
            } as EntityValidator<Any>

            val groups = parameter.findAnnotations(Valid::class).single().value.toSet()

            buildList {
                if (groups.contains(Group.CREATE)) {
                    val result = validator.onCreate().validate(value)
                    if (!result.isValid) addAll(result.errors)
                }
                if (groups.contains(Group.UPDATE)) {
                    val result = validator.onUpdate().validate(value)
                    if (!result.isValid) addAll(result.errors)
                }
            }
        }

        return if (errors.isEmpty()) null else {
            ValidationException(errors.map { ValidationError(it.dataPath, it.message) })
        }
    }
}
