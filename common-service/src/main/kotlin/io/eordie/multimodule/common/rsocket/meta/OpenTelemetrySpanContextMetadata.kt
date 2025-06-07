package io.eordie.multimodule.common.rsocket.meta

import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.writeFully
import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.api.trace.SpanId
import io.opentelemetry.api.trace.TraceFlags
import io.opentelemetry.api.trace.TraceId
import io.opentelemetry.api.trace.TraceState
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.core.CustomMimeType
import io.rsocket.kotlin.core.MimeType
import io.rsocket.kotlin.metadata.Metadata
import io.rsocket.kotlin.metadata.MetadataReader
import kotlinx.io.Buffer
import kotlinx.io.Sink

@OptIn(ExperimentalMetadataApi::class)
class OpenTelemetrySpanContextMetadata(val spanContext: SpanContext) : Metadata {
    override val mimeType: MimeType = Reader.mimeType

    override fun close() = Unit

    override fun Sink.writeSelf() {
        writeFully(spanContext.traceIdBytes)
        writeFully(spanContext.spanIdBytes)
        writeByte(spanContext.traceFlags.asByte())

        val entries = spanContext.traceState.asMap().entries
        writeInt(entries.size)
        entries.forEach { (key, value) ->
            val keyBytes = key.encodeToByteArray()
            val valueBytes = value.encodeToByteArray()
            writeInt(keyBytes.size)
            writeFully(keyBytes)
            writeInt(valueBytes.size)
            writeFully(valueBytes)
        }
    }

    companion object Reader : MetadataReader<OpenTelemetrySpanContextMetadata> {
        override val mimeType: MimeType = CustomMimeType("message/x.opentelementry.tracing.v0")
        override fun Buffer.read(): OpenTelemetrySpanContextMetadata {
            val traceId = TraceId.fromBytes(readBytes(16))
            val spanId = SpanId.fromBytes(readBytes(8))
            val traceFlags = TraceFlags.fromByte(readByte())

            val builder = TraceState.builder()
            var entriesSize = readInt()
            while (entriesSize-- > 0) {
                builder.put(readBytes(readInt()).decodeToString(), readBytes(readInt()).decodeToString())
            }

            val spanContext = SpanContext.create(traceId, spanId, traceFlags, builder.build())
            return OpenTelemetrySpanContextMetadata(spanContext)
        }
    }
}
