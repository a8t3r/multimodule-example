package io.eordie.multimodule.organization.repository

import io.eordie.multimodule.example.repository.KBaseFactory
import io.eordie.multimodule.organization.models.OrganizationEmployeeModel
import io.eordie.multimodule.organization.models.OrganizationEmployeeModelDraft
import jakarta.inject.Singleton
import java.util.*

@Singleton
class OrganizationEmployeeFactory : KBaseFactory<OrganizationEmployeeModel, UUID, Any>(
    OrganizationEmployeeModel::class,
    OrganizationEmployeeModelDraft.`$`.type
)