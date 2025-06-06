package io.eordie.multimodule.common.rsocket.meta

import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
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
class AuthenticationMetadata(val details: AuthenticationDetails) : Metadata {

    override val mimeType: MimeType = Reader.mimeType

    override fun close() = Unit

    override fun Sink.writeSelf() {
        proto.encodeToPacketBuilder(details, type)(this)
    }

    companion object Reader : MetadataReader<AuthenticationMetadata> {
        private val type = AuthenticationDetails::class.createType()
        private val proto = ProtobufPayloadBuilder()

        override val mimeType: MimeType = CustomMimeType("Authentication")
        override fun Buffer.read(): AuthenticationMetadata {
            val size = readInt()
            val details = proto.decodeFromUnpackedBytes(readBytes(size), type) as AuthenticationDetails
            return AuthenticationMetadata(details)
        }
    }
}
