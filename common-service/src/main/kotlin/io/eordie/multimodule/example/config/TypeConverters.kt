package io.eordie.multimodule.example.config

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
            val collection: C = collector.invoke((obj.array as Array<T>))
            Optional.of(collection)
        }
    }

    @Bean
    @Primary
    fun <T : Any> pgArrayToListTypeConverter(): TypeConverter<PgArray, List<T>> = collectionConverter(Array<T>::toList)

    @Bean
    @Primary
    fun <T : Any> pgArrayToSetTypeConverter(): TypeConverter<PgArray, Set<T>> = collectionConverter(Array<T>::toSet)
}
