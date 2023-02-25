package io.eordie.multimodule.organization.config

import io.eordie.multimodule.example.config.UUIDScalarProvider
import io.eordie.multimodule.organization.models.OrganizationDomainModelProps
import io.eordie.multimodule.organization.models.OrganizationMemberModelProps
import io.eordie.multimodule.organization.models.OrganizationModelProps
import io.eordie.multimodule.organization.models.OrganizationRoleModelProps
import io.eordie.multimodule.organization.models.UserAttributeModelProps
import io.eordie.multimodule.organization.models.UserModelProps
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import org.babyfish.jimmer.meta.ImmutableProp
import org.babyfish.jimmer.sql.runtime.ScalarProvider
import java.util.*

@Factory
class ScalarProviderFactory {

    @Bean
    fun stringIdScalarProvider(): ScalarProvider<UUID, String> {
        return object : UUIDScalarProvider() {
            override fun getHandledProps(): Collection<ImmutableProp> {
                return listOf(
                    OrganizationModelProps.ID.unwrap(),
                    OrganizationDomainModelProps.ID.unwrap(),
                    OrganizationDomainModelProps.ORGANIZATION_ID.unwrap(),
                    UserModelProps.ID.unwrap(),
                    OrganizationRoleModelProps.ORGANIZATION_ID.unwrap(),
                    UserAttributeModelProps.ID.unwrap(),
                    UserAttributeModelProps.USER_ID.unwrap(),
                    OrganizationMemberModelProps.ID.unwrap(),
                    OrganizationMemberModelProps.USER_ID.unwrap(),
                    OrganizationMemberModelProps.ORGANIZATION_ID.unwrap()
                )
            }
        }
    }
}
