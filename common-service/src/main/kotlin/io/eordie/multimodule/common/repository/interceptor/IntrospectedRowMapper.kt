package io.eordie.multimodule.common.repository.interceptor

import io.eordie.multimodule.contracts.utils.getIntrospection
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

class IntrospectedRowMapper<T : Any>(
    private val mappedClass: Class<T>,
    private val conversionService: ConversionService
) : RowMapper<T> {

    override fun mapRow(rs: ResultSet, rowNum: Int): T? {
        val constructor = getIntrospection<T>(mappedClass.kotlin).constructor
        val parameters = constructor.arguments.map { argument ->
            val value = rs.getObject(argument.name)
            if (argument.type == List::class.java && value == null) {
                ArrayList<Any>()
            } else {
                conversionService.convert(value, Argument.of(argument.asType())).orElseThrow()
            }
        }
        return constructor.instantiate(*parameters.toTypedArray())
    }
}
