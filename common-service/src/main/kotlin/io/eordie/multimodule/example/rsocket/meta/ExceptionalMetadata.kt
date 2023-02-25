package io.eordie.multimodule.example.rsocket.meta

import io.eordie.multimodule.example.contracts.basic.exception.ExceptionDefinition
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.pool.*
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.core.CustomMimeType
import io.rsocket.kotlin.core.MimeType
import io.rsocket.kotlin.metadata.Metadata
import io.rsocket.kotlin.metadata.MetadataReader
import kotlin.reflect.full.createType

@OptIn(ExperimentalMetadataApi::class)
class ExceptionalMetadata(
    val definition: ExceptionDefinition
) : Metadata {

    companion object Reader : MetadataReader<ExceptionalMetadata> {
        val proto = ProtobufPayloadBuilder()
        val type = ExceptionDefinition::class.createType()

        override val mimeType = CustomMimeType("exceptional")

        override fun ByteReadPacket.read(pool: ObjectPool<ChunkBuffer>): ExceptionalMetadata {
            val size = readInt()
            val definition = proto.decodeFromUnpackedBytes(readBytes(size), type) as ExceptionDefinition
            return ExceptionalMetadata(definition)
        }
    }

    override val mimeType: MimeType = Reader.mimeType

    override fun close() = Unit

    override fun BytePacketBuilder.writeSelf() {
        proto.encodeToPacketBuilder(definition, type)(this)
    }
}
