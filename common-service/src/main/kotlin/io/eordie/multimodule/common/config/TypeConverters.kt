package io.eordie.multimodule.common.config

import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.utils.JsonModule
import io.eordie.multimodule.contracts.utils.uncheckedCast
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.core.convert.TypeConverter
import org.postgresql.jdbc.PgArray
import java.util.*
import kotlin.reflect.KFunction1

@Factory
class TypeConverters {

    private fun <T, C : Collection<T>, M> collectionConverter(
        collector: KFunction1<Array<T>, C>
    ): TypeConverter<PgArray, C> where M : MutableCollection<T> {
        return TypeConverter<PgArray, C> { obj, _, _ ->
            val collection: C = collector.invoke((obj.array.uncheckedCast()))
            Optional.of(collection)
        }
    }

    @Bean
    @Primary
    fun authDetailsFromBytesTypeConverter(): TypeConverter<ByteArray, AuthenticationDetails> =
        TypeConverter<ByteArray, AuthenticationDetails> { data, _, _ ->
            Optional.of(JsonModule.getInstance().decodeFromString(String(data)))
        }

    @Bean
    @Primary
    fun <T : Any> pgArrayToListTypeConverter(): TypeConverter<PgArray, List<T>> = collectionConverter(Array<T>::toList)

    @Bean
    @Primary
    fun <T : Any> pgArrayToSetTypeConverter(): TypeConverter<PgArray, Set<T>> = collectionConverter(Array<T>::toSet)

    @Bean
    @Primary
    fun <T : Any> optionalToListTypeConverter(): TypeConverter<Optional<T>, List<T>> =
        TypeConverter<Optional<T>, List<T>> { obj, _, _ ->
            obj.map { listOf(it) }.or { Optional.of(emptyList()) }
        }
}
