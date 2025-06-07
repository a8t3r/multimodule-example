package io.eordie.multimodule.common.utils

import io.eordie.multimodule.contracts.utils.ProtobufModule
import io.eordie.multimodule.contracts.utils.safeCast
import io.micronaut.core.serialize.ObjectSerializer
import io.micronaut.core.type.Argument
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@Suppress("unused")
@OptIn(ExperimentalSerializationApi::class)
class ProtobufObjectSerializer : ObjectSerializer {

    private val proto = ProtobufModule.getInstance()

    override fun serialize(value: Any, outputStream: OutputStream) {
        val serializer = proto.serializersModule.serializer(value::class.java)
        outputStream.write(proto.encodeToByteArray(serializer, value))
    }

    override fun <T : Any> deserialize(bytes: ByteArray, requiredType: Argument<T>): Optional<T> {
        val serializer: KSerializer<T> = safeCast(proto.serializersModule.serializer(requiredType.type))
        val value = proto.decodeFromByteArray(serializer, bytes)
        return Optional.of(value)
    }

    override fun <T : Any> deserialize(inputStream: InputStream, requiredType: Class<T>): Optional<T> {
        throw UnsupportedOperationException()
    }
}
