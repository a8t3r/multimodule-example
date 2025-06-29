package io.eordie.multimodule.common.config

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.babyfish.jimmer.meta.ImmutableProp
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.sql.kt.cache.KSimpleBinder
import java.time.Duration

internal class GuavaCacheBinder<K : Any, V : Any> : KSimpleBinder<K, V> {

    private val cache: Cache<K, V> = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.ofHours(1))
        .maximumSize(1000)
        .build()

    override fun deleteAll(keys: Collection<K>, reason: Any?) {
        cache.invalidateAll(keys)
    }

    override fun getAll(keys: Collection<K>): Map<K, V> {
        return cache.getAllPresent(keys).toMutableMap()
    }

    override fun setAll(map: Map<K, V>) {
        cache.invalidateAll(map.keys)
        cache.putAll(map)
    }

    override fun prop(): ImmutableProp? = null
    override fun type(): ImmutableType? = null
}
