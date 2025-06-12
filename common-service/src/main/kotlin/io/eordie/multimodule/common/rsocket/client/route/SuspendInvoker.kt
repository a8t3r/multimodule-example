package io.eordie.multimodule.common.rsocket.client.route

import io.eordie.multimodule.contracts.utils.uncheckedCast
import io.micronaut.context.BeanLocator
import io.micronaut.kotlin.context.getBean
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Tracer
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.reflect.KClass

abstract class SuspendInvoker(val kotlinIFace: KClass<*>, val beanLocator: BeanLocator) {
    abstract val isRemote: Boolean
    abstract suspend fun invoke(method: Method, context: CoroutineContext, arguments: Array<Any?>): Any?

    protected val tracer: Tracer by lazy {
        val telemetry = beanLocator.getBean<OpenTelemetry>()
        telemetry.tracerBuilder(kotlinIFace.java.simpleName).build()
    }

    fun <C : Any> proxy(): C {
        val contract = kotlinIFace.java
        val contracts = arrayOf(contract, Synthesized::class.java)
        val tag = if (isRemote) "Remote" else "Local"
        return Proxy.newProxyInstance(contract.classLoader, contracts) { _, method, arguments ->
            when (method.name) {
                "toString" -> "$tag proxy over [${contract.simpleName}]"
                else -> {
                    val continuation = arguments.last() as? Continuation<*>
                    if (continuation != null) {
                        val updatedArguments = if (isRemote) arguments.copyOf(arguments.size - 1) else arguments
                        val call: suspend () -> Any? = {
                            invoke(method, continuation.context, updatedArguments)
                        }

                        call.startCoroutine(
                            Continuation(continuation.context) { continuation.resumeWith(it.uncheckedCast()) }
                        )
                        kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
                    } else {
                        val index = arguments
                            .indexOfFirst { it != null && CoroutineContext::class.java.isAssignableFrom(it.javaClass) }
                            .takeUnless { it < 0 }

                        val context = if (index == null) EmptyCoroutineContext else {
                            val context = requireNotNull(arguments[index]) as CoroutineContext
                            if (isRemote) {
                                arguments[index] = null
                            }
                            context
                        }

                        runBlocking {
                            invoke(method, context, arguments)
                        }
                    }
                }
            }
        }.uncheckedCast()
    }
}
