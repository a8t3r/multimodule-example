package io.eordie.multimodule.common.repository.loader

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import io.eordie.multimodule.common.repository.KBaseFactory
import io.eordie.multimodule.common.rsocket.client.route.LocalRoute
import io.eordie.multimodule.common.rsocket.client.route.SuspendInvoker
import io.eordie.multimodule.common.rsocket.client.rsocket.KubernetesRSocketFactory
import io.eordie.multimodule.common.rsocket.client.rsocket.LocalRSocketFactory
import io.eordie.multimodule.common.rsocket.client.rsocket.RSocketLocalFactory
import io.eordie.multimodule.contracts.basic.loader.EntityLoader
import io.micronaut.context.BeanLocator
import io.micronaut.inject.qualifiers.Qualifiers.byName
import jakarta.inject.Singleton
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@Singleton
class GenericEntityLoader(
    private val beanLocator: BeanLocator
) {

    private val isSimulateRemoteRouting: Boolean = false

    private val routesCache: LoadingCache<Pair<Class<*>, Class<*>>, EntityLoaderRoute> = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .build(object : CacheLoader<Pair<Class<*>, Class<*>>, EntityLoaderRoute>() {
            override fun load(key: Pair<Class<*>, Class<*>>): EntityLoaderRoute {
                return EntityLoaderRoute(key.first.kotlin, key.second.kotlin, beanLocator, socketFactory)
            }
        })

    private val socketFactory: RSocketLocalFactory by lazy {
        if (isSimulateRemoteRouting) LocalRSocketFactory(beanLocator) else KubernetesRSocketFactory(beanLocator)
    }

    suspend fun load(objectType: Class<*>, ids: List<Any>): Map<Any, Any> {
        val factory = findFactory(objectType)
        return loadByFactory(factory, coroutineContext, ids, objectType)
    }

    fun createLoader(idType: Class<*>, entityType: Class<*>): EntityLoader<Any, Any> {
        return findFactory(entityType)
            .map<SuspendInvoker> {
                LocalRoute(EntityLoader::class, beanLocator, it)
            }
            .orElseGet {
                routesCache.get(idType to entityType)
            }
            .proxy()
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun loadByFactory(
        factory: Optional<KBaseFactory<*, *, *, *, *>>,
        context: CoroutineContext,
        ids: List<Any>,
        objectType: Class<*>
    ): Map<Any, Any> {
        return when {
            ids.isEmpty() -> emptyMap()
            factory.isPresent && !isSimulateRemoteRouting -> {
                withContext(context) {
                    factory.get().load(ids as List<Nothing>)
                }
            }
            else -> {
                val firstId = ids.first()
                val route = routesCache.get(firstId::class.java to objectType)
                route.load(ids, context)
            }
        } as Map<Any, Any>
    }

    private fun findFactory(objectClass: Class<*>): Optional<KBaseFactory<*, *, *, *, *>> {
        val simpleName = objectClass.simpleName
        return beanLocator.findBean(KBaseFactory::class.java, byName("${simpleName}Factory"))
            .or { beanLocator.findBean(KBaseFactory::class.java, byName("${simpleName}sFactory")) }
    }
}
