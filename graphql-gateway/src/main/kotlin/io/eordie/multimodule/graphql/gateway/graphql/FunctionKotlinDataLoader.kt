package io.eordie.multimodule.graphql.gateway.graphql

import com.expediagroup.graphql.dataloader.KotlinDataLoader
import graphql.GraphQLContext
import io.eordie.multimodule.common.rsocket.client.getServiceInterface
import io.eordie.multimodule.contracts.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend

class FunctionKotlinDataLoader(
    private val instance: Query,
    private val function: KFunction<*>,
    private val cacheProvider: CacheProvider
) : KotlinDataLoader<Any, Any?> {

    companion object {
        private const val MAX_BATCH_SIZE = 100
    }

    private val defaultOptions by lazy {
        val builder = DataLoaderOptions.newOptions()
            .setMaxBatchSize(MAX_BATCH_SIZE)

        val cache = cacheProvider.findCache(function)
        if (cache != null) {
            builder
                .setCachingEnabled(true)
                .setValueCache(cache)
        }

        builder
    }

    private val isBatchedFunction = function.returnType.classifier == Map::class

    override val dataLoaderName =
        (instance::class.getServiceInterface(Query::class) ?: instance::class).simpleName + ":" + function.name

    override fun getDataLoader(graphQLContext: GraphQLContext): DataLoader<Any, Any?> {
        return DataLoaderFactory.newDataLoader(
            { ids, _ ->
                CoroutineScope(graphQLContext.newCoroutineContext()).future {
                    buildResult(ids)
                }
            },
            defaultOptions
        )
    }

    // build groups of invocation arguments for batching calls
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
            // perform invocation for each unique group of arguments
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
            // if batching unsupported then perform naive invocations
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
