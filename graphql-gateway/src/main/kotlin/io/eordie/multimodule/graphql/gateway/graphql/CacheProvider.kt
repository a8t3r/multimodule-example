package io.eordie.multimodule.graphql.gateway.graphql

import io.eordie.multimodule.contracts.annotations.Cached
import io.micronaut.cache.SyncCache
import io.opentelemetry.api.OpenTelemetry
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

@Singleton
class CacheProvider(
    @param:Named("dataloader")
    private val cache: SyncCache<*>,
    private val openTelemetry: OpenTelemetry
) {

    private val defined = mutableMapOf<Class<*>, RedisValueCache<Any, Any>>()

    fun findCache(function: KFunction<*>): RedisValueCache<Any, Any>? {
        val cached = function.findAnnotation<Cached>() ?: return null

        val targetClass = cached.value.takeUnless { it == Unit::class }?.java
            ?: error("target class must be specified for cached annotation")

        return getCache(targetClass)
    }

    fun findCache(targetClass: Class<*>): RedisValueCache<Any, Any>? {
        val cachedAnnotation = targetClass.getAnnotation(Cached::class.java)
        return if (cachedAnnotation == null) null else {
            getCache(targetClass)
        }
    }

    private fun getCache(targetClass: Class<*>): RedisValueCache<Any, Any> {
        return defined.getOrPut(targetClass) {
            RedisValueCache(
                openTelemetry
                    .tracerBuilder("${targetClass.simpleName}Cache")
                    .build(),
                cache,
                targetClass
            )
        }
    }
}
