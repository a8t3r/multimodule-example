package io.eordie.multimodule.graphql.gateway.graphql

import com.expediagroup.graphql.generator.execution.FunctionDataFetcher
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

class TracingFunctionDataFetcher(
    private val target: Any?,
    private val interfaceFn: KFunction<*>,
    private val parametersTransformer: ParametersTransformer
) : FunctionDataFetcher(target, interfaceFn) {

    private val interfaceParameterIndex = interfaceFn.valueParameters.associateBy { it.index }

    override fun get(environment: DataFetchingEnvironment): Any? {
        val instance: Any? = target ?: environment.getSource<Any?>()
        val instanceParameter = interfaceFn.instanceParameter

        return if (instance != null && instanceParameter != null) {
            val parameterValues = getParameters(interfaceFn, environment)
                .plus(instanceParameter to instance)

            if (interfaceFn.isSuspend) {
                runSuspendingFunction(environment, parameterValues)
            } else {
                runBlockingFunction(environment, parameterValues)
            }
        } else {
            null
        }
    }

    override fun runSuspendingFunction(
        environment: DataFetchingEnvironment,
        parameterValues: Map<KParameter, Any?>
    ): CompletableFuture<Any?> {
        return CoroutineScope(environment.graphQlContext.newCoroutineContext()).future {
            interfaceFn.callSuspendBy(parameterValues)
        }
    }

    private fun runBlockingFunction(
        environment: DataFetchingEnvironment,
        parameterValues: Map<KParameter, Any?>
    ): Any? {
        val coroutineContext = environment.graphQlContext.newCoroutineContext()
        val parameters = parameterValues.mapValues { (key, value) ->
            if (key.type.classifier != CoroutineContext::class) value else coroutineContext
        }

        return super.runBlockingFunction(parameters)
    }

    override fun mapParameterToValue(param: KParameter, environment: DataFetchingEnvironment): Pair<KParameter, Any?>? {
        return parametersTransformer.mapParameterToValue(param, environment) ?: run {
            if (environment.containsArgument(param.name)) {
                val interfaceParam = requireNotNull(interfaceParameterIndex[param.index])
                super.mapParameterToValue(interfaceParam, environment)?.let { (_, value) ->
                    param to value
                }
            } else if (param.isOptional || param.type.isMarkedNullable) {
                param to null
            } else {
                error("unknown parameter type: $param")
            }
        }
    }
}
