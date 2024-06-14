package io.eordie.multimodule.organization.management.config

import io.eordie.multimodule.organization.management.models.OrganizationPositionModelProps
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import org.babyfish.jimmer.meta.ImmutableProp
import org.babyfish.jimmer.sql.runtime.ScalarProvider

@Factory
class ScalarProviderFactory {

    @Bean
    fun intArrayScalarProvider(): ScalarProvider<List<Int>, Array<Int>> {
        return object : ScalarProvider<List<Int>, Array<Int>> {
            override fun toScalar(sqlValue: Array<Int>): List<Int> = sqlValue.toList()
            override fun toSql(scalarValue: List<Int>): Array<Int> = scalarValue.toList().toTypedArray()
            override fun getHandledProps(): Collection<ImmutableProp> {
                return listOf(
                    OrganizationPositionModelProps.ROLE_IDS.unwrap()
                )
            }
        }
    }
}
