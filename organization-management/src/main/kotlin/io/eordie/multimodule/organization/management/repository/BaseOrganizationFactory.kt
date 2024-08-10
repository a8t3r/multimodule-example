package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.repository.Convertable
import io.eordie.multimodule.common.repository.KBaseFactory
import io.eordie.multimodule.contracts.basic.BasePermission
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.organization.models.acl.ResourceAcl
import io.eordie.multimodule.contracts.utils.Roles
import org.babyfish.jimmer.sql.kt.ast.expression.value
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

abstract class BaseOrganizationFactory<T : Convertable<C>, C : Any, ID, F : Any>(
    entityType: KClass<T>
) : KBaseFactory<T, C, ID, F>(entityType) where ID : Any, ID : Comparable<ID> {

    override val datasourceName = "keycloak"

    abstract val organizationId: KProperty1<T, UUID>
    open val viewRoles: Set<Roles> = emptySet()
    open val manageRoles: Set<Roles> = setOf(Roles.MANAGE_ORGANIZATION)

    override fun ResourceAcl.listPredicates(table: KNonNullTable<T>) = listOf(
        when {
            hasOrganizationRole(Roles.MANAGE_ORGANIZATIONS) -> value(true)
            hasAllOrganizationRoles(viewRoles) -> {
                table.get<UUID>(organizationId.name) valueIn allOrganizationIds
            }
            else -> value(false)
        }
    )

    override suspend fun calculatePermissions(acl: ResourceAcl, value: T): Set<Permission> {
        return buildSet {
            if (acl.hasOrganizationRole(Roles.VIEW_ORGANIZATIONS)) {
                add(BasePermission.VIEW)
            }
            if (acl.hasOrganizationRole(Roles.MANAGE_ORGANIZATIONS)) {
                add(BasePermission.MANAGE)
                add(BasePermission.PURGE)
            }

            val viewOrganizationIds = acl.organizationsWithRole(viewRoles)
            val writeOrganizationIds = acl.organizationsWithRole(manageRoles)

            val organizationId = organizationId.get(value)
            if (organizationId in acl.allOrganizationIds) {
                if (organizationId in viewOrganizationIds) {
                    add(BasePermission.VIEW)
                }
                if (organizationId in writeOrganizationIds) {
                    add(BasePermission.MANAGE)
                    add(BasePermission.PURGE)
                }
            }
        }
    }
}
