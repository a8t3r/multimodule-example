package io.eordie.multimodule.contracts.identitymanagement.models

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import java.util.*

@Introspected
@Serializable
data class LocaleBinding(val country: String, val language: String) {

    companion object {
        fun default(): LocaleBinding {
            val locale = Locale.getDefault()
            return LocaleBinding(locale.country, locale.language)
        }
    }
}
