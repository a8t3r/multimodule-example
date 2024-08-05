package io.eordie.multimodule.common.rsocket.client.route

import io.eordie.multimodule.common.security.context.AuthenticationContextElement
import io.eordie.multimodule.common.utils.extendWith
import io.eordie.multimodule.contracts.annotations.Secured
import io.eordie.multimodule.contracts.annotations.Valid
import io.micronaut.context.BeanLocator
import io.opentelemetry.extension.kotlin.getOpenTelemetryContext
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.kotlinFunction

class LocalRoute(
    kotlinIFace: KClass<*>,
    beanLocator: BeanLocator,
    private val beanInstance: Any
) : SuspendInvoker(kotlinIFace, beanLocator) {

    override val isRemote: Boolean = false

    override suspend fun invoke(method: Method, context: CoroutineContext, arguments: Array<Any?>): Any? {
        beforeInvocation(method, context, arguments)

        val routeId = "${kotlinIFace.simpleName}:${method.name}"
        val span = tracer.spanBuilder("(Client) $routeId")
            .setParent(context.getOpenTelemetryContext())
            .extendWith(context)
            .setAttribute("Local", true)
            .startSpan()

        return try {
            method.invoke(beanInstance, *arguments)
        } catch (ignored: InvocationTargetException) {
            throw ignored.targetException
        } finally {
            span.end()
        }
    }

    private suspend fun beforeInvocation(
        method: Method,
        context: CoroutineContext,
        arguments: Array<Any?>
    ) {
        val securityException = AuthorizationCheck.check(
            method.declaringClass.getAnnotation(Secured::class.java),
            method.getAnnotationsByType(Secured::class.java).toList(),
            context[AuthenticationContextElement]?.details
        )
        if (securityException != null) throw securityException

        val validationException = ValidationCheck.check(
            context,
            beanLocator,
            requireNotNull(method.kotlinFunction).valueParameters.zip(arguments)
                .mapNotNull { (param, value) ->
                    value?.takeIf { param.findAnnotations(Valid::class).isNotEmpty() }?.let { param to it }
                }
        )
        if (validationException != null) throw validationException
    }
}
