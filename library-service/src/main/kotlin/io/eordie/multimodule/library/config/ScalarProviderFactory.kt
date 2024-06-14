package io.eordie.multimodule.library.config

import io.eordie.multimodule.library.models.BookModelProps
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import org.babyfish.jimmer.meta.ImmutableProp
import org.babyfish.jimmer.sql.runtime.ScalarProvider
import java.util.*

@Factory
class ScalarProviderFactory {

    @Bean
    fun bookAuthorScalarProvider(): ScalarProvider<List<UUID>, Array<UUID>> {
        return object : ScalarProvider<List<UUID>, Array<UUID>> {
            override fun toScalar(sqlValue: Array<UUID>): List<UUID> = sqlValue.toList()
            override fun toSql(scalarValue: List<UUID>): Array<UUID> = scalarValue.toList().toTypedArray()
            override fun getHandledProps(): Collection<ImmutableProp> {
                return listOf(
                    BookModelProps.AUTHOR_IDS.unwrap()
                )
            }
        }
    }
}
