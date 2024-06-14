package io.eordie.multimodule.common.config

import io.eordie.multimodule.contracts.basic.event.MutationEvent
import io.eordie.multimodule.contracts.utils.JsonModule
import io.micronaut.configuration.kafka.serde.CompositeSerdeRegistry
import io.micronaut.configuration.kafka.serde.SerdeRegistry
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.core.type.Argument
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.common.serialization.Serializer
import java.io.ByteArrayInputStream
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

@Factory
@Requires(property = "kafka.enabled", value = "true")
class KafkaConfig {

    companion object {
        private val proto = JsonModule.getInstance()
    }

    class ProtoSerde<T>(private val serializer: KSerializer<T>) : Serde<T> {
        override fun serializer(): Serializer<T> {
            return Serializer { _, data ->
                proto.encodeToString(serializer, data).toByteArray()
            }
        }

        override fun deserializer(): Deserializer<T> {
            return Deserializer { _, data ->
                proto.decodeFromStream(serializer, ByteArrayInputStream(data))
            }
        }
    }

    @Bean
    @Replaces(CompositeSerdeRegistry::class)
    fun serdeSerdeRegistry(): SerdeRegistry {
        return object : SerdeRegistry {
            override fun <T : Any?> pickDeserializer(argument: Argument<T>): Deserializer<T> {
                return if (!argument.isAssignableFrom(MutationEvent::class.java)) {
                    super.pickDeserializer(argument)
                } else {
                    val generic = argument.typeVariables.getValue("T").type
                    val serializer = proto.serializersModule.serializer(
                        MutationEvent::class.createType(listOf(KTypeProjection.invariant(generic.kotlin.createType())))
                    ) as KSerializer<T>
                    ProtoSerde(serializer).deserializer()
                }
            }

            override fun <T : Any?> getSerde(type: Class<T>): Serde<T> {
                return if (type.isAssignableFrom(String::class.java)) {
                    StringSerde() as Serde<T>
                } else {
                    val serializer = proto.serializersModule.serializer(type) as KSerializer<T>
                    ProtoSerde(serializer)
                }
            }
        }
    }
}
