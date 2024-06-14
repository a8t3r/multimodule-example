package io.eordie.multimodule.common.rsocket.meta

import io.eordie.multimodule.contracts.utils.ProtobufModule
import io.ktor.utils.io.core.*
import io.rsocket.kotlin.payload.Payload
import io.rsocket.kotlin.payload.PayloadBuilder
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

@OptIn(ExperimentalSerializationApi::class)
class ProtobufPayloadBuilder {

    private val proto = ProtobufModule.getInstance()

    private fun getSerializer(type: KType): KSerializer<Any?> {
        return proto.serializersModule.serializer(type)
    }

    fun <T : Any> decodeFromPayload(payload: Payload, targetType: KClass<T>): T? =
        decodeFromPayload(payload, targetType.createType()) as T?

    fun decodeFromPayload(payload: Payload, targetType: KType): Any? {
        val size = payload.data.readInt()
        return decodeFromUnpackedBytes(payload.data.readBytes(size), targetType)
    }

    fun decodeFromUnpackedBytes(data: ByteArray, targetType: KType): Any? {
        return if (data.isEmpty() && targetType.isMarkedNullable) null else {
            proto.decodeFromByteArray(
                getSerializer(targetType),
                data
            )
        }
    }

    fun decodeFromPayload(payload: Payload, types: List<KType>, context: CoroutineContext? = null): List<Any?> {
        val data = payload.data
        return types.map { type ->
            val size = data.readInt()
            when {
                size == 0 && type.isMarkedNullable -> null
                size == 0 && type.classifier == CoroutineContext::class -> context
                else -> {
                    val bytes = data.readBytes(size)
                    proto.decodeFromByteArray(getSerializer(type), bytes)
                }
            }
        }
    }

    fun <T : Any> encodeToPayload(value: T?, targetType: KType): Payload = buildPayload {
        data {
            encodeToPacketBuilder(value, targetType)(this)
        }
    }

    fun encodeToPayload(arguments: List<Any?>, types: List<KType>): Payload = buildPayload {
        encodeToBuilder(arguments, types)(this)
    }

    fun encodeToBuilder(arguments: List<Any?>, types: List<KType>): PayloadBuilder.() -> Unit = {
        data {
            arguments.zip(types).forEach { (value, type) ->
                encodeToPacketBuilder(value, type)(this)
            }
        }
    }

    fun encodeToPacketBuilder(value: Any?, type: KType): BytePacketBuilder.() -> Unit = {
        if (value == null) writeInt(0) else {
            val data = proto.encodeToByteArray(getSerializer(type), value)
            writeInt(data.size)
            writeFully(data)
        }
    }
}
