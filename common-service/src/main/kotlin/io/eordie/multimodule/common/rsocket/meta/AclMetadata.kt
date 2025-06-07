package io.eordie.multimodule.common.rsocket.meta

import io.eordie.multimodule.contracts.organization.models.acl.EmployeeAcl
import io.eordie.multimodule.contracts.utils.safeCast
import io.ktor.utils.io.core.readBytes
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.core.CustomMimeType
import io.rsocket.kotlin.core.MimeType
import io.rsocket.kotlin.metadata.Metadata
import io.rsocket.kotlin.metadata.MetadataReader
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

@OptIn(ExperimentalMetadataApi::class)
class AclMetadata(val acl: List<EmployeeAcl>) : Metadata {

    override val mimeType: MimeType = Reader.mimeType

    override fun close() = Unit

    override fun Sink.writeSelf() {
        proto.encodeToPacketBuilder(acl, type)(this)
    }

    companion object Reader : MetadataReader<AclMetadata> {
        private val type = List::class.createType(listOf(KTypeProjection.invariant(EmployeeAcl::class.createType())))
        private val proto = ProtobufPayloadBuilder()

        override val mimeType: MimeType = CustomMimeType("Acl")
        override fun Buffer.read(): AclMetadata {
            val size = readInt()
            val acl: List<EmployeeAcl> = safeCast(proto.decodeFromUnpackedBytes(readBytes(size), type))
            return AclMetadata(acl)
        }
    }
}
