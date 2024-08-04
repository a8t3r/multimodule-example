package io.eordie.multimodule.organization.management.repository

import io.eordie.multimodule.common.filter.accept
import io.eordie.multimodule.common.repository.KBaseFactory
import io.eordie.multimodule.common.repository.ResourceAcl
import io.eordie.multimodule.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.contracts.organization.models.User
import io.eordie.multimodule.contracts.organization.models.UsersFilter
import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.organization.management.models.UserModel
import io.eordie.multimodule.organization.management.models.`attributes?`
import io.eordie.multimodule.organization.management.models.email
import io.eordie.multimodule.organization.management.models.emailVerified
import io.eordie.multimodule.organization.management.models.enabled
import io.eordie.multimodule.organization.management.models.firstName
import io.eordie.multimodule.organization.management.models.id
import io.eordie.multimodule.organization.management.models.lastName
import io.eordie.multimodule.organization.management.models.membership
import io.eordie.multimodule.organization.management.models.name
import io.eordie.multimodule.organization.management.models.organization
import io.eordie.multimodule.organization.management.models.organizationId
import io.eordie.multimodule.organization.management.models.value
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.and
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*

@Singleton
class UserFactory : KBaseFactory<UserModel, User, UUID, UsersFilter>(
    UserModel::class
) {

    override val datasourceName = "keycloak"

    override fun sortingExpressions(table: KNonNullTable<UserModel>): List<KPropExpression<out Comparable<*>>> {
        return listOf(
            table.firstName,
            table.lastName,
            table.email,
            table.emailVerified,
            table.enabled,
            table.id
        )
    }

    override fun listPredicates(acl: ResourceAcl, table: KNonNullTable<UserModel>) = listOfNotNull(
        table.firstName.isNotNull(),
        table.lastName.isNotNull(),
        table.enabled eq true,
        table.membership {
            organizationId valueIn acl.allOrganizationIds
        }.takeUnless { acl.hasAnyOrganizationRole(Roles.VIEW_USERS) }
    )

    private fun observeAttributes(
        filter: StringLiteralFilter,
        attributeName: String,
        table: KNonNullTable<UserModel>
    ): KNonNullExpression<Boolean>? {
        return with(table.asTableEx().`attributes?`) {
            and(
                or(name.isNull(), name eq attributeName),
                value.accept(filter)
            )
        }
    }

    override fun ResourceAcl.toPredicates(
        filter: UsersFilter,
        table: KNonNullTable<UserModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.id.accept(filter.id),
            table.firstName.accept(filter.firstName),
            table.lastName.accept(filter.lastName),
            table.email.accept(filter.email),
            table.emailVerified.accept(filter.emailVerified),
            table.membership { organization.accept(filter.organization) },
            filter.phoneNumber?.let {
                observeAttributes(it, "phoneNumber", table)
            },
            filter.phoneNumberVerified?.transformToStringLiteral()?.let {
                observeAttributes(it, "phoneNumberVerified", table)
            }
        )
    }
}
