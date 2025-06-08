package io.eordie.multimodule.common.config

import io.eordie.multimodule.common.utils.typeArguments
import io.ktor.util.moveToByteArray
import io.lettuce.core.RedisClient
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.micronaut.configuration.kafka.serde.SerdeRegistry
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Replaces
import io.micronaut.inject.ArgumentInjectionPoint
import jakarta.inject.Singleton
import org.apache.kafka.common.serialization.Serde
import java.nio.ByteBuffer

@Factory
class RedisConfig {

    @Primary
    @Singleton
    @Bean(preDestroy = "close")
    @Replaces(StatefulRedisPubSubConnection::class)
    fun <K, V> redisPubSubConnection(
        registry: SerdeRegistry,
        @Primary redisClient: RedisClient,
        injection: ArgumentInjectionPoint<*, *>
    ): StatefulRedisPubSubConnection<K, V> {
        val (keyType, valueType) = injection.typeArguments<K, V>("K", "V")

        val codec = redisCodec(registry.getSerde(keyType), registry.getSerde(valueType))
        return redisClient.connectPubSub(codec)
    }

    private fun <K, V> redisCodec(keySerde: Serde<K>, valueSerde: Serde<V>): RedisCodec<K, V> {
        return object : RedisCodec<K, V> {
            private val topic = ""
            override fun decodeKey(bytes: ByteBuffer): K =
                keySerde.deserializer().deserialize(topic, bytes.moveToByteArray())

            override fun encodeKey(key: K): ByteBuffer =
                ByteBuffer.wrap(keySerde.serializer().serialize(topic, key))

            override fun decodeValue(bytes: ByteBuffer): V =
                valueSerde.deserializer().deserialize(topic, bytes.moveToByteArray())

            override fun encodeValue(value: V): ByteBuffer =
                ByteBuffer.wrap(valueSerde.serializer().serialize(topic, value))
        }
    }
}
