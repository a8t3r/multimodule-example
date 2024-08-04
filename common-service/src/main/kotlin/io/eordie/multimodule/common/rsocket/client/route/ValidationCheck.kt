package io.eordie.multimodule.common.rsocket.client.route

import io.eordie.multimodule.common.rsocket.context.AuthenticationContextElement
import io.eordie.multimodule.common.validation.EntityValidator
import io.eordie.multimodule.contracts.annotations.Group
import io.eordie.multimodule.contracts.annotations.Valid
import io.eordie.multimodule.contracts.basic.exception.ValidationError
import io.eordie.multimodule.contracts.basic.exception.ValidationException
import io.micronaut.context.BeanLocator
import io.micronaut.core.type.Argument
import kotlinx.coroutines.withContext
import org.valiktor.ConstraintViolationException
import org.valiktor.i18n.mapToMessage
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.javaType

object ValidationCheck {

    fun ConstraintViolationException.toErrors(coroutineContext: CoroutineContext): List<ValidationError> {
        val binding = coroutineContext[AuthenticationContextElement.Key]?.details?.locale
        val locale = if (binding == null) Locale.getDefault() else {
            Locale.forLanguageTag(binding.language)
        }

        return constraintViolations
            .mapToMessage(locale = locale)
            .map { violation ->
                ValidationError(
                    violation.property.takeIf { it.isNotEmpty() },
                    violation.message,
                    violation.constraint.name,
                    violation.constraint.messageParams
                )
            }
    }

    private suspend fun check(context: CoroutineContext, block: suspend () -> Unit): List<ValidationError> {
        return try {
            block.invoke()
            emptyList()
        } catch (e: ConstraintViolationException) {
            e.toErrors(context)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun check(
        context: CoroutineContext,
        beanLocator: BeanLocator,
        arguments: List<Pair<KParameter, Any>>
    ): ValidationException? {
        val errors = arguments.flatMap { (parameter, value) ->
            val validator = beanLocator.findBean(
                Argument.of(EntityValidator::class.java, Argument.of(parameter.type.javaType))
            ).orElseThrow {
                error("no validator found for type ${parameter.type}")
            } as EntityValidator<Any>

            val groups = parameter.findAnnotations(Valid::class).single().value.toSet()

            withContext(context) {
                buildList {
                    if (groups.contains(Group.CREATE)) {
                        addAll(check(context) { validator.onCreate(value) })
                    }
                    if (groups.contains(Group.UPDATE)) {
                        addAll(check(context) { validator.onUpdate(value) })
                    }
                }
            }
        }

        return if (errors.isEmpty()) null else {
            ValidationException(errors)
        }
    }
}
