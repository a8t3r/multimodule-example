package io.eordie.multimodule.example.rsocket.meta

import io.eordie.multimodule.example.contracts.identitymanagement.models.AuthenticationDetails
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
class AuthenticationMetadata(val details: AuthenticationDetails) : Metadata {

    override val mimeType: MimeType = Reader.mimeType

    override fun close() = Unit

    override fun BytePacketBuilder.writeSelf() {
        proto.encodeToPacketBuilder(details, type)(this)
    }

    companion object Reader : MetadataReader<AuthenticationMetadata> {
        private val type = AuthenticationDetails::class.createType()
        private val proto = ProtobufPayloadBuilder()

        override val mimeType: MimeType = CustomMimeType("Authentication")

        override fun ByteReadPacket.read(pool: ObjectPool<ChunkBuffer>): AuthenticationMetadata {
            val size = readInt()
            val details = proto.decodeFromUnpackedBytes(readBytes(size), type) as AuthenticationDetails
            return AuthenticationMetadata(details)
        }
    }
}
