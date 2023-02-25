package io.eordie.multimodule.organization.repository

import io.eordie.multimodule.example.contracts.basic.filters.StringLiteralFilter
import io.eordie.multimodule.example.contracts.organization.models.UsersFilter
import io.eordie.multimodule.example.filter.accept
import io.eordie.multimodule.example.repository.KBaseFactory
import io.eordie.multimodule.organization.models.UserModel
import io.eordie.multimodule.organization.models.UserModelDraft
import io.eordie.multimodule.organization.models.`attributes?`
import io.eordie.multimodule.organization.models.email
import io.eordie.multimodule.organization.models.emailVerified
import io.eordie.multimodule.organization.models.enabled
import io.eordie.multimodule.organization.models.firstName
import io.eordie.multimodule.organization.models.id
import io.eordie.multimodule.organization.models.lastName
import io.eordie.multimodule.organization.models.membership
import io.eordie.multimodule.organization.models.name
import io.eordie.multimodule.organization.models.organization
import io.eordie.multimodule.organization.models.organizationId
import io.eordie.multimodule.organization.models.value
import io.eordie.multimodule.organization.utils.isOrganizationMember
import jakarta.inject.Singleton
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.and
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import java.util.*
import kotlin.coroutines.CoroutineContext

@Singleton
class UserFactory : KBaseFactory<UserModel, UUID, UsersFilter>(
    UserModel::class,
    UserModelDraft.`$`.type
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

    override fun listPredicates(
        context: CoroutineContext,
        table: KNonNullTable<UserModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.membership {
                organizationId.isOrganizationMember(context)
            }
        )
    }

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

    override fun toPredicates(
        context: CoroutineContext,
        filter: UsersFilter,
        table: KNonNullTable<UserModel>
    ): List<KNonNullExpression<Boolean>> {
        return listOfNotNull(
            table.enabled eq true,
            table.id.accept(filter.id),
            table.firstName.accept(filter.firstName),
            table.lastName.accept(filter.lastName),
            table.email.accept(filter.email),
            table.emailVerified.accept(filter.emailVerified),
            filter.organization?.let {
                table.membership {
                    registry.toPredicates(context, it, organization.asTableEx())
                }
            },
            filter.phoneNumber?.let {
                observeAttributes(it, "phoneNumber", table)
            },
            filter.phoneNumberVerified?.transformToStringLiteral()?.let {
                observeAttributes(it, "phoneNumberVerified", table)
            }
        )
    }
}
