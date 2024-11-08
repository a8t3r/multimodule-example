package io.eordie.multimodule.common.rsocket.meta

import io.eordie.multimodule.contracts.basic.paging.SelectionSet
import io.ktor.utils.io.core.*
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.core.CustomMimeType
import io.rsocket.kotlin.core.MimeType
import io.rsocket.kotlin.internal.BufferPool
import io.rsocket.kotlin.metadata.Metadata
import io.rsocket.kotlin.metadata.MetadataReader
import kotlin.reflect.full.createType

@OptIn(ExperimentalMetadataApi::class)
class SelectionSetMetadata(val selectionSet: SelectionSet) : Metadata {
    override val mimeType: MimeType = Reader.mimeType

    override fun close() = Unit

    override fun BytePacketBuilder.writeSelf() {
        proto.encodeToPacketBuilder(selectionSet, type)(this)
    }

    companion object Reader : MetadataReader<SelectionSetMetadata> {
        private val type = SelectionSet::class.createType()
        private val proto = ProtobufPayloadBuilder()

        override val mimeType: MimeType = CustomMimeType("SelectionSet")

        override fun ByteReadPacket.read(pool: BufferPool): SelectionSetMetadata {
            val size = readInt()
            val selectionSet = proto.decodeFromUnpackedBytes(readBytes(size), type) as SelectionSet
            return SelectionSetMetadata(selectionSet)
        }
    }
}
