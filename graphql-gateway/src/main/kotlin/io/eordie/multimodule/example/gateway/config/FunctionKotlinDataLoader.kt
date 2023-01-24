package io.eordie.multimodule.example.gateway.config

import com.expediagroup.graphql.dataloader.KotlinDataLoader
import io.eordie.multimodule.example.contracts.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.superclasses
import kotlin.reflect.full.valueParameters

class FunctionKotlinDataLoader(
    private val instance: Query,
    private val function: KFunction<*>
) : KotlinDataLoader<Any, Any?> {

    companion object {
        private const val maxBatchSize = 200
    }

    private val defaultOptions = DataLoaderOptions.newOptions()
        .setMaxBatchSize(maxBatchSize)
        .setCachingEnabled(true)

    private val isBatchedFunction = function.valueParameters.first().type.arguments.isNotEmpty()

    override val dataLoaderName = instance::class.superclasses.first().simpleName + ":" + function.name

    override fun getDataLoader(): DataLoader<Any, Any?> = DataLoaderFactory.newDataLoader(
        { ids ->
            CoroutineScope(EmptyCoroutineContext).future {
                buildResult(ids)
            }
        },
        defaultOptions
    )

    private suspend fun buildResult(keys: List<Any>): MutableList<Any?> {
        val result = mutableListOf<Any?>().also { list -> repeat(keys.size) { list.add(null) } }

        // each key is array of (id, argument1, ..., argumentN)
        val preparedArgs = keys.mapIndexed { index, key ->
            val inputKey = (key as Array<*>).withIndex().associateBy({ it.index }, { it.value })
            // for function parameters: skip instance and id parameters, e.g. drop(2)
            // for inputKey: skip only id parameters, e.g. drop(1)
            val argumentValues = function.parameters.drop(2).map { inputKey[it.index - 1] }
            argumentValues to IndexedValue(index, key.first())
        }

        if (isBatchedFunction) {
            preparedArgs.groupBy({ it.first }, { it.second })
                .entries
                .forEach { (argumentValues, indexedKeys) ->
                    val invocationKeys = indexedKeys.map { it.value }
                    val response = callSuspend<Map<Any, Any?>>(invocationKeys, argumentValues)
                    indexedKeys.forEach {
                        result[it.index] = response[it.value]
                    }
                }
        } else {
            preparedArgs.forEach { (argumentValues, indexedValue) ->
                val key = indexedValue.value
                result[indexedValue.index] = callSuspend(key, argumentValues)
            }
        }

        return result
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> callSuspend(id: Any?, argumentValues: List<Any?>): T {
        return function.callSuspend(instance, id, *argumentValues.toTypedArray()) as T
    }
}
