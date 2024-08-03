package io.eordie.multimodule.common.config

import io.lettuce.core.api.StatefulRedisConnection
import io.micronaut.configuration.lettuce.cache.RedisCache
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.sql.cache.spi.AbstractRemoteValueBinder
import java.time.Duration

class RedisValueBinder<K, V>(
    immutableType: ImmutableType,
    cache: RedisCache
) : AbstractRemoteValueBinder<K, V>(
    immutableType,
    null,
    null,
    null,
    Duration.ofMinutes(10),
    20
) {

    private val operations = (cache.nativeCache as StatefulRedisConnection<ByteArray, ByteArray>).sync()

    override fun matched(reason: Any?): Boolean = reason == "redis"

    override fun deleteAllSerializedKeys(serializedKeys: MutableList<String>) {
        operations.del(*serializedKeys.map { it.toByteArray() }.toTypedArray())
    }

    override fun read(keys: MutableCollection<String>): MutableList<ByteArray?> {
        return operations.mget(*keys.map { it.toByteArray() }.toTypedArray())
            .mapTo(MutableList(keys.size) { null }) { if (it.hasValue()) it.value else null }
    }

    override fun write(map: MutableMap<String, ByteArray>) {
        val payload = map.mapKeys { it.key.toByteArray() }
        operations.mset(payload)
        payload.keys.forEach { operations.expire(it, Duration.ofMillis(nextExpireMillis())) }
    }
}
