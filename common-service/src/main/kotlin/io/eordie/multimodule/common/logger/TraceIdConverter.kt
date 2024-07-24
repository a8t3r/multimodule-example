package io.eordie.multimodule.common.logger

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.TraceId

class TraceIdConverter : ClassicConverter() {
    override fun convert(event: ILoggingEvent): String {
        val traceId = Span.current()?.spanContext?.traceId
        return if (traceId == TraceId.getInvalid()) "" else "trace_id=$traceId"
    }
}
