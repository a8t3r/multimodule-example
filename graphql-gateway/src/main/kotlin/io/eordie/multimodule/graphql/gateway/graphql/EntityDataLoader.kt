package io.eordie.multimodule.graphql.gateway.graphql

import com.expediagroup.graphql.dataloader.KotlinDataLoader
import graphql.GraphQLContext
import io.eordie.multimodule.common.repository.loader.GenericEntityLoader
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions

@Singleton
class EntityDataLoader(
    private val cacheProvider: CacheProvider,
    private val loader: GenericEntityLoader
) : KotlinDataLoader<Any, Any> {
    override val dataLoaderName = "EntityDataLoader"

    private val defaultOptions = DataLoaderOptions.newOptions()
        .setMaxBatchSize(100)
        .setCachingEnabled(true)

    override fun getDataLoader(graphQLContext: GraphQLContext): DataLoader<Any, Any> {
        return DataLoaderFactory.newDataLoader(
            { ids, _ ->
                CoroutineScope(graphQLContext.newCoroutineContext()).future {
                    buildResult(ids)
                }
            },
            defaultOptions
        )
    }

    private suspend fun buildResult(keys: List<Any>): MutableList<Any?> {
        val result = mutableListOf<Any?>().also { list -> repeat(keys.size) { list.add(null) } }
        val arguments = keys.map { it as Array<*> }
            .map { (it[0] as Any) to (it[1] as String) }

        val index: Map<Pair<Any, String>, MutableSet<Int>> = arguments
            .mapIndexed { index, pair -> pair to index }
            .fold(mutableMapOf()) { acc, (pair, index) ->
                val set = acc.computeIfAbsent(pair) { mutableSetOf() }
                set.add(index)
                acc
            }

        val groupsByType = arguments.groupBy({ it.second }, { it.first })

        groupsByType.forEach { (type, ids) ->
            val objectType = Class.forName(type)
            val cache = cacheProvider.findCache(objectType)
            val values = if (cache == null) loader.load(objectType, ids) else {
                loadByCache(cache, objectType, ids)
            }

            values.forEach { (key, value) ->
                requireNotNull(index[key to type]).forEach { index ->
                    result[index] = value
                }
            }
        }

        return result
    }

    private suspend fun loadByCache(
        cache: RedisValueCache<Any, Any>,
        objectType: Class<*>,
        ids: List<Any>
    ): Map<Any, Any> {
        val (cachedValues, missing) = cache.getBlocking(ids)
        return if (missing.isEmpty()) cachedValues else {
            val missingValues = loader.load(objectType, missing).also {
                cache.setBlocking(it)
            }
            cachedValues + missingValues
        }
    }
}
