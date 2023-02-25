package io.eordie.multimodule.example.contracts.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object JsonModule {
    fun getInstance(): Json {
        return Json {
            serializersModule = SerializersModule {
                contextual(UUID::class, UUIDSerializer)
                contextual(LocalDateTime::class, LocalDateTimeSerializer)
            }
        }
    }

    object UUIDSerializer : KSerializer<UUID> {
        override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): UUID {
            return UUID.fromString(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: UUID) {
            encoder.encodeString(value.toString())
        }
    }

    object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: LocalDateTime) {
            encoder.encodeString(value.format(formatter))
        }

        override fun deserialize(decoder: Decoder): LocalDateTime {
            return LocalDateTime.parse(decoder.decodeString(), formatter)
        }
    }
}
