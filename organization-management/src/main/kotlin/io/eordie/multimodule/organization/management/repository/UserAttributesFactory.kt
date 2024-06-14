package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.repository.KFactoryImpl
import jakarta.inject.Singleton
import java.util.*

@Singleton
class UserAttributesFactory : KFactoryImpl<io.eordie.multimodule.organization.management.models.UserAttributeModel, UUID>(
    io.eordie.multimodule.organization.management.models.UserAttributeModel::class
) {

    override val datasourceName = "keycloak"
}
