package io.eordie.multimodule.organization.controllers

import io.eordie.multimodule.example.contracts.organization.services.UserMutations
import io.eordie.multimodule.organization.models.UserModel
import io.eordie.multimodule.organization.models.by
import io.eordie.multimodule.organization.repository.UserAttributesFactory
import io.eordie.multimodule.organization.repository.UserFactory
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*

@Singleton
open class UserMutationsController(
    private val users: UserFactory,
    private val userAttributes: UserAttributesFactory
) : UserMutations {

    override suspend fun switchOrganization(userId: UUID, organizationId: UUID): Boolean {
        val fetcher = newFetcher(UserModel::class).by { membership { organizationId() } }
        val supportedOrganizationIds = users.findById(userId, fetcher)?.organizationIds().orEmpty()

        return if (!supportedOrganizationIds.contains(organizationId)) false else {
            userAttributes.switchOrganization(userId, organizationId)
        }
    }
}
