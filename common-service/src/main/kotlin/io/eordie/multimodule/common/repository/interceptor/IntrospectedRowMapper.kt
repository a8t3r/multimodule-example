package io.eordie.multimodule.common.repository.interceptor

import io.eordie.multimodule.contracts.utils.getIntrospection
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import org.apache.commons.lang3.StringUtils
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

class IntrospectedRowMapper<T : Any>(
    mappedClass: Class<T>,
    private val conversionService: ConversionService
) : RowMapper<T> {

    private val constructor = getIntrospection<T>(mappedClass.kotlin).constructor
    private lateinit var columnIndex: Map<String, Int>
    private lateinit var argumentIndex: Map<Argument<*>, String>

    private fun String.simplify(): String = StringUtils.remove(this, '_').uppercase()

    override fun mapRow(rs: ResultSet, rowNum: Int): T? {
        if (rowNum == 0) {
            val metaData = rs.metaData
            this.argumentIndex = constructor.arguments.associateWith { it.name.simplify() }
            this.columnIndex = (1..metaData.columnCount).associateBy { metaData.getColumnName(it).simplify() }
        }

        val parameters = argumentIndex.entries.map { (argument, columnName) ->
            val columnIndex = columnIndex.getValue(columnName)
            val value = rs.getObject(columnIndex)
            // nullable array value
            if (argument.type == List::class.java && value == null) {
                ArrayList<Any>()
            } else {
                conversionService.convert(value, Argument.of(argument.asType())).orElseThrow()
            }
        }
        return constructor.instantiate(*parameters.toTypedArray())
    }
}
