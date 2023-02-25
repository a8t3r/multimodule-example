package io.eordie.multimodule.organization.repository

import io.eordie.multimodule.example.repository.KBaseFactory
import io.eordie.multimodule.organization.models.OrganizationDepartmentModel
import io.eordie.multimodule.organization.models.OrganizationDepartmentModelDraft
import jakarta.inject.Singleton
import java.util.*

@Singleton
class OrganizationDepartmentFactory : KBaseFactory<OrganizationDepartmentModel, UUID, Any>(
    OrganizationDepartmentModel::class,
    OrganizationDepartmentModelDraft.`$`.type
)
