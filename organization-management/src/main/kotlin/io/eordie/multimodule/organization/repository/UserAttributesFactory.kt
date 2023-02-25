package io.eordie.multimodule.organization.repository

import io.eordie.multimodule.example.repository.KFactoryImpl
import io.eordie.multimodule.organization.models.UserAttributeModel
import io.eordie.multimodule.organization.models.UserAttributeModelDraft
import io.eordie.multimodule.organization.models.name
import io.eordie.multimodule.organization.models.userId
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import java.util.*

@Singleton
class UserAttributesFactory : KFactoryImpl<UserAttributeModel, UUID>(
    UserAttributeModel::class,
    UserAttributeModelDraft.`$`.type
) {

    override val datasourceName = "keycloak"
    private val activeOrganizationName = "org.ro.active"

    suspend fun switchOrganization(userId: UUID, organizationId: UUID): Boolean {
        val attribute = findOneBySpecification {
            where(
                table.name eq activeOrganizationName,
                table.userId eq userId
            )
        }

        return saveIf<UserAttributeModelDraft>(attribute?.id) { isNew, value ->
            if (isNew) {
                value.name = activeOrganizationName
                value.userId = userId
            }

            val previous = value.value
            value.value = organizationId.toString()
            previous != value.value
        }.second
    }
}
