package io.eordie.multimodule.contracts.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object JsonModule {
    fun getInstance(): Json {
        return Json {
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
                contextual(UUID::class, UUIDSerializer)
                contextual(OffsetDateTime::class, OffsetDateTimeSerializer)
                contextual(RoleSet::class, ProtobufModule.RoleSetSerializer)
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

    object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
        private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: OffsetDateTime) {
            encoder.encodeString(value.format(formatter))
        }

        override fun deserialize(decoder: Decoder): OffsetDateTime {
            return OffsetDateTime.parse(decoder.decodeString(), formatter)
        }
    }
}
