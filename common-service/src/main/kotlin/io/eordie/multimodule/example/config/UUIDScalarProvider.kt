package io.eordie.multimodule.example.config

import org.babyfish.jimmer.sql.runtime.ScalarProvider
import java.util.*

open class UUIDScalarProvider : ScalarProvider<UUID, String> {
    override fun toScalar(sqlValue: String): UUID = UUID.fromString(sqlValue)
    override fun toSql(scalarValue: UUID): String = scalarValue.toString()
}
