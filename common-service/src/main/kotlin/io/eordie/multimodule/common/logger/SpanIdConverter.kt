package io.eordie.multimodule.common.logger

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanId

class SpanIdConverter : ClassicConverter() {
    override fun convert(event: ILoggingEvent): String {
        val spanId = Span.current()?.spanContext?.spanId
        return if (spanId == SpanId.getInvalid()) "" else "span_id=$spanId"
    }
}
