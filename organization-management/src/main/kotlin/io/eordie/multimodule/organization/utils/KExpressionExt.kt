package io.eordie.multimodule.organization.utils

import io.eordie.multimodule.example.contracts.utils.Roles
import io.eordie.multimodule.example.rsocket.context.getAuthenticationContext
import io.eordie.multimodule.example.utils.ifMissingRole
import org.babyfish.jimmer.sql.kt.ast.expression.KExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.value
import java.util.*
import kotlin.coroutines.CoroutineContext

fun KExpression<UUID>.isOrganizationMember(context: CoroutineContext): KNonNullExpression<Boolean>? {
    val organizationProperty = this
    return context.ifMissingRole(Roles.MANAGE_ORGANIZATIONS) {
        context.getAuthenticationContext().currentOrganizationId?.let {
            organizationProperty.eq(it)
        } ?: value(false)
    }
}
