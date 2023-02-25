package io.eordie.multimodule.example.repository.loader

import io.eordie.multimodule.example.contracts.basic.loader.EntityLoader
import io.eordie.multimodule.example.repository.KBaseFactory
import io.eordie.multimodule.example.rsocket.client.invocation.LocalRoute
import io.eordie.multimodule.example.rsocket.client.invocation.RemoteRoute
import io.eordie.multimodule.example.rsocket.client.invocation.SuspendInvoker
import io.micronaut.context.BeanLocator
import io.micronaut.inject.qualifiers.Qualifiers.byName
import jakarta.inject.Singleton
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KFunction3
import kotlin.reflect.jvm.javaMethod

@Singleton
class GenericEntityLoader(
    private val beanLocator: BeanLocator
) {
    private val remoteRoute = RemoteRoute(EntityLoader::class, beanLocator)

    private val method: KFunction3<EntityLoader<Any, Any>, CoroutineContext, List<Any>, Map<Any, Any>> =
        EntityLoader<Any, Any>::load

    suspend fun load(simpleName: String, ids: List<Any>): Map<Any, Any> {
        val factory = findFactory(simpleName)
        return loadByFactory(factory, coroutineContext, ids, simpleName)
    }

    suspend fun load(entityType: Class<*>, ids: List<Any>): Map<Any, Any> {
        return load(entityType.simpleName, ids)
    }

    fun createLoader(entityType: Class<*>): EntityLoader<Any, Any> {
        return findFactory(entityType.simpleName)
            .map<SuspendInvoker> { LocalRoute(EntityLoader::class, beanLocator, it) }
            .orElseGet { RemoteRoute(EntityLoader::class, beanLocator, entityType.simpleName) }
            .proxy()
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun loadByFactory(
        factory: Optional<KBaseFactory<*, *, *>>,
        context: CoroutineContext,
        ids: List<Any>,
        simpleName: String
    ): Map<Any, Any> {
        return if (factory.isPresent) {
            factory.get().load(context, ids as List<Nothing>)
        } else {
            remoteRoute.invoke(
                simpleName,
                requireNotNull(method.javaMethod),
                context,
                arrayOf(ids)
            )
        } as Map<Any, Any>
    }

    private fun findFactory(simpleName: String): Optional<KBaseFactory<*, *, *>> {
        return beanLocator.findBean(KBaseFactory::class.java, byName("${simpleName}Factory"))
            .or { beanLocator.findBean(KBaseFactory::class.java, byName("${simpleName}sFactory")) }
    }
}
