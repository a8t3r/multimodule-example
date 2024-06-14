package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.repository.KFactoryImpl
import io.eordie.multimodule.organization.management.models.OrganizationMemberModel
import jakarta.inject.Singleton
import java.util.*

@Singleton
class OrganizationMemberFactory : KFactoryImpl<OrganizationMemberModel, UUID>(
    OrganizationMemberModel::class
) {
    override val datasourceName = "keycloak"
}
