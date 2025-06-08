package io.eordie.multimodule.common.utils

import io.lettuce.core.pubsub.RedisPubSubAdapter

class RedisEventListener<V>(private val processor: (V) -> Unit) : RedisPubSubAdapter<String, V>() {
    override fun message(channel: String, message: V) {
        processor.invoke(message)
    }
}
