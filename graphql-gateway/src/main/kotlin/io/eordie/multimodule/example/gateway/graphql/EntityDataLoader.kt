package io.eordie.multimodule.example.gateway.graphql

import com.expediagroup.graphql.dataloader.KotlinDataLoader
import graphql.GraphQLContext
import io.eordie.multimodule.example.repository.loader.GenericEntityLoader
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions

@Singleton
class EntityDataLoader(private val loader: GenericEntityLoader) : KotlinDataLoader<Any, Any> {
    override val dataLoaderName = "EntityDataLoader"

    private val defaultOptions = DataLoaderOptions.newOptions()
        .setMaxBatchSize(100)
        .setCachingEnabled(true)

    override fun getDataLoader(graphQLContext: GraphQLContext): DataLoader<Any, Any> {
        val context = graphQLContext.openTelemetryElement() + graphQLContext.authenticationElementOrEmpty()
        return DataLoaderFactory.newDataLoader(
            { ids, _ ->
                CoroutineScope(context).future {
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
            val values = loader.load(type, ids)
            values.forEach { (key, value) ->
                requireNotNull(index[key to type]).forEach { index ->
                    result[index] = value
                }
            }
        }

        return result
    }
}
