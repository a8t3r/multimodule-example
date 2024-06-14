package io.eordie.multimodule.graphql.gateway.graphql

import io.micronaut.cache.SyncCache
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import org.dataloader.Try
import org.dataloader.ValueCache
import java.util.concurrent.CompletableFuture
import kotlin.time.measureTimedValue

class RedisValueCache<ID, T>(
    private val tracer: Tracer,
    private val cache: SyncCache<*>,
    private val targetClass: Class<out T>
) : ValueCache<ID, T> {

    private val asyncCache = cache.async()
    private val notFound = CompletableFuture.failedStage<T>(RuntimeException("not found"))

    private val prefix = targetClass.simpleName

    private fun keyOf(id: ID): String {
        val str = if (id is Array<*>) id.contentToString() else id.toString()
        return "$prefix:$str"
    }

    private fun <R> makeSpan(method: String, block: () -> R): R {
        val span = tracer.spanBuilder("value cache: $method")
            .setParent(Context.current())
            .startSpan()

        try {
            val (result, duration) = measureTimedValue {
                block.invoke()
            }
            val executionTime = Attributes.of(AttributeKey.longKey("execution_time"), duration.inWholeMilliseconds)
            span.addEvent("completed", executionTime)
            return result
        } finally {
            span.end()
        }
    }

    fun getBlocking(keys: Collection<ID>): Pair<Map<ID, T>, List<ID>> {
        return makeSpan("getBlocking(${keys.size})") {
            val missing: MutableList<ID> = mutableListOf()
            val cachedValues = keys.mapNotNull { id ->
                val value = cache.get(keyOf(id), targetClass).orElse(null)
                if (value != null) id to value else {
                    missing.add(id)
                    null
                }
            }.toMap()

            cachedValues to missing
        }
    }

    fun setBlocking(values: Map<ID, T>) {
        makeSpan("setBlocking(${values.size})") {
            values.entries.forEach { (key, value) ->
                cache.put(keyOf(key), value)
            }
        }
    }

    override fun getValues(keys: MutableList<ID>): CompletableFuture<MutableList<Try<T>>> {
        return makeSpan("getValues(${keys.size})") {
            super.getValues(keys)
        }
    }

    override fun setValues(keys: MutableList<ID>, values: MutableList<T>): CompletableFuture<MutableList<T>> {
        return makeSpan("setValues(${keys.size})") {
            super.setValues(keys, values)
        }
    }

    override fun get(key: ID): CompletableFuture<T> {
        return asyncCache.get(keyOf(key), targetClass).thenCompose { result ->
            result.map { CompletableFuture.completedStage(it) }.orElse(notFound)
        }
    }

    override fun set(key: ID, value: T): CompletableFuture<T> {
        return asyncCache.put(keyOf(key), value).thenApply { value }
    }

    @Suppress("ForbiddenVoid")
    override fun delete(key: ID): CompletableFuture<Void> {
        return asyncCache.invalidate(keyOf(key)).thenAccept { }
    }

    @Suppress("ForbiddenVoid")
    override fun clear(): CompletableFuture<Void> {
        return makeSpan("clear") {
            asyncCache.invalidateAll().thenAccept { }
        }
    }
}
