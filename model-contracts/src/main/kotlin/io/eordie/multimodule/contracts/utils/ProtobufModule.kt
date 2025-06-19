package io.eordie.multimodule.contracts.utils

import io.eordie.multimodule.contracts.basic.BasePermission
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.basic.exception.AccessDeniedException
import io.eordie.multimodule.contracts.basic.exception.BaseRuntimeException
import io.eordie.multimodule.contracts.basic.exception.EntityNotFoundException
import io.eordie.multimodule.contracts.basic.exception.UnauthenticatedException
import io.eordie.multimodule.contracts.basic.exception.UnexpectedInvocationException
import io.eordie.multimodule.contracts.basic.exception.ValidationException
import io.eordie.multimodule.contracts.organization.models.acl.BindingCriterion
import io.eordie.multimodule.contracts.organization.models.acl.ByFarmCriterion
import io.eordie.multimodule.contracts.organization.models.acl.ByRegionCriterion
import io.eordie.multimodule.contracts.organization.models.acl.GlobalCriterion
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.protobuf.ProtoBuf
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
object ProtobufModule {

    private val INSTANCE = ProtoBuf {
        encodeDefaults = false
        serializersModule = SerializersModule {
            contextual(UUID::class, UUIDSerializer)
            contextual(OffsetDateTime::class, OffsetDateTimeSerializer)
            contextual(RoleSet::class, RoleSetSerializer)

            polymorphic(BaseRuntimeException::class) {
                subclass(ValidationException::class)
                subclass(AccessDeniedException::class)
                subclass(UnauthenticatedException::class)
                subclass(EntityNotFoundException::class)
                subclass(UnexpectedInvocationException::class)
            }

            polymorphic(Permission::class) {
                subclass(BasePermission::class)
            }

            polymorphic(BindingCriterion::class) {
                subclass(GlobalCriterion::class)
                subclass(ByFarmCriterion::class)
                subclass(ByRegionCriterion::class)
            }
        }
    }

    fun getInstance(): ProtoBuf = INSTANCE

    object UUIDSerializer : KSerializer<UUID> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("UUID") {
            element<Long>("m")
            element<Long>("l")
        }

        override fun deserialize(decoder: Decoder): UUID {
            return decoder.decodeStructure(descriptor) {
                var mostBits: Long = -1
                var leastBits: Long = -1
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> mostBits = decodeLongElement(descriptor, 0)
                        1 -> leastBits = decodeLongElement(descriptor, 1)
                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Unexpected index: $index")
                    }
                }
                require(mostBits != -1L && leastBits != -1L)
                UUID(mostBits, leastBits)
            }
        }

        override fun serialize(encoder: Encoder, value: UUID) {
            encoder.encodeStructure(descriptor) {
                encodeLongElement(descriptor, 0, value.mostSignificantBits)
                encodeLongElement(descriptor, 1, value.leastSignificantBits)
            }
        }
    }

    object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.LONG)

        override fun serialize(encoder: Encoder, value: OffsetDateTime) {
            encoder.encodeLong(value.toInstant().toEpochMilli())
        }

        override fun deserialize(decoder: Decoder): OffsetDateTime {
            return OffsetDateTime.ofInstant(Instant.ofEpochMilli(decoder.decodeLong()), ZoneOffset.UTC)
        }
    }

    object RoleSetSerializer : KSerializer<RoleSet> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RoleSet", PrimitiveKind.LONG)

        override fun serialize(encoder: Encoder, value: RoleSet) {
            val encodedValue = value.fold(0L) { acc, role ->
                acc.or(1L shl role.ordinal)
            }
            encoder.encodeLong(encodedValue)
        }

        override fun deserialize(decoder: Decoder): RoleSet {
            val decodedValue = decoder.decodeLong()
            return Roles.entries.fold(mutableListOf<Roles>()) { acc, value ->
                if ((decodedValue and (1L shl value.ordinal)) != 0L) {
                    acc.add(value)
                }
                acc
            }.asRoleSet()
        }
    }
}
