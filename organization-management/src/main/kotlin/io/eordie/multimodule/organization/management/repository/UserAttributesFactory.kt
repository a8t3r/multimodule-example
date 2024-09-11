package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.repository.KFactoryImpl
import io.eordie.multimodule.organization.management.models.UserAttributeModel
import io.eordie.multimodule.organization.management.models.UserAttributeModelDraft
import jakarta.inject.Singleton
import java.util.*

@Singleton
class UserAttributesFactory : KFactoryImpl<UserAttributeModel, UserAttributeModelDraft, UUID>(
    UserAttributeModel::class
) {

    override val datasourceName = "keycloak"
}
