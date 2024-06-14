package io.eordie.multimodule.contracts

import io.eordie.multimodule.contracts.utils.OffsetDateTimeStr
import io.micronaut.core.annotation.Introspected
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private fun formatDatetime(datetime: OffsetDateTime, pattern: String?): String {
    val formatter = if (pattern == null) DateTimeFormatter.ISO_OFFSET_DATE_TIME else {
        DateTimeFormatter.ofPattern(pattern)
    }
    return formatter.format(datetime)
}

@Introspected
interface Auditable {
    val deleted: Boolean
    val createdAt: OffsetDateTimeStr
    val updatedAt: OffsetDateTimeStr

    fun createdAt(pattern: String? = null): String = formatDatetime(createdAt, pattern)
    fun updatedAt(pattern: String? = null): String = formatDatetime(updatedAt, pattern)
}
