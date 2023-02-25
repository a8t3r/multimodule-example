package io.eordie.multimodule.example.contracts

import io.eordie.multimodule.example.contracts.utils.OffsetDateTimeStr
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private fun formatDatetime(datetime: OffsetDateTime, pattern: String?): String {
    val formatter = if (pattern == null) DateTimeFormatter.ISO_OFFSET_DATE_TIME else {
        DateTimeFormatter.ofPattern(pattern)
    }
    return formatter.format(datetime)
}

interface Auditable {
    val deleted: Boolean
    val createdAt: OffsetDateTimeStr
    val updatedAt: OffsetDateTimeStr

    fun createdAt(pattern: String? = null): String = formatDatetime(createdAt, pattern)
    fun updatedAt(pattern: String? = null): String = formatDatetime(updatedAt, pattern)
}
