package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.contracts.organization.models.extra.OrganizationPublicInformation
import io.eordie.multimodule.organization.management.models.OrganizationPublicInformationModel
import jakarta.inject.Singleton
import java.util.*

@Singleton
class OrganizationPublicInformationFactory :
    BaseOrganizationFactory<OrganizationPublicInformationModel, OrganizationPublicInformation, UUID, Any>(
        OrganizationPublicInformationModel::class
    ) {
    override val organizationId = OrganizationPublicInformationModel::organizationId
}
