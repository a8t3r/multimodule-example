package io.eordie.multimodule.contracts.basic

import io.micronaut.core.annotation.Introspected
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Headers
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@Introspected
data class RequestHeaders(private val data: Map<String, List<String>>) : Headers {
    override fun getAll(name: CharSequence): List<String> = data[name] ?: emptyList()
    override fun get(name: CharSequence): String? = data[name]?.firstOrNull()
    override fun names(): Set<String> = data.keys
    override fun values(): Collection<List<String>> = data.values
    override fun <T : Any> get(name: CharSequence, conversionContext: ArgumentConversionContext<T>): Optional<T> {
        val value = get(name)
        return if (value == null) Optional.empty<T>() else {
            ConversionService.SHARED.convert(value, conversionContext)
        }
    }
}
