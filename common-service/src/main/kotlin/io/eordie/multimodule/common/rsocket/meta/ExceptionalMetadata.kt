package io.eordie.multimodule.common.rsocket.meta

import io.eordie.multimodule.contracts.basic.exception.BaseRuntimeException
import io.ktor.utils.io.core.readBytes
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.core.CustomMimeType
import io.rsocket.kotlin.core.MimeType
import io.rsocket.kotlin.metadata.Metadata
import io.rsocket.kotlin.metadata.MetadataReader
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlin.reflect.full.createType

@OptIn(ExperimentalMetadataApi::class)
class ExceptionalMetadata(
    val ex: BaseRuntimeException
) : Metadata {

    companion object Reader : MetadataReader<ExceptionalMetadata> {
        val proto = ProtobufPayloadBuilder()
        val type = BaseRuntimeException::class.createType()

        override val mimeType = CustomMimeType("exceptional")
        override fun Buffer.read(): ExceptionalMetadata {
            val size = readInt()
            val ex = proto.decodeFromUnpackedBytes(readBytes(size), type) as BaseRuntimeException
            return ExceptionalMetadata(ex)
        }
    }

    override val mimeType: MimeType = Reader.mimeType

    override fun close() = Unit

    override fun Sink.writeSelf() {
        proto.encodeToPacketBuilder(ex, type)(this)
    }
}
