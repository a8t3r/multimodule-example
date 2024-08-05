package io.eordie.multimodule.common.security.context

import io.eordie.multimodule.contracts.basic.exception.UnauthenticatedException
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.identitymanagement.models.LocaleBinding
import io.eordie.multimodule.contracts.utils.Roles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class AuthenticationContextElement(
    val details: AuthenticationDetails
) : CoroutineContext.Element {

    companion object Key : CoroutineContext.Key<AuthenticationContextElement>

    override val key: CoroutineContext.Key<*> = Key
}

fun CoroutineContext.getAuthenticationContext(): AuthenticationDetails {
    return get(AuthenticationContextElement)?.details ?: throw UnauthenticatedException()
}

suspend fun getAuthentication() = coroutineContext.getAuthenticationContext()

val systemContext = AuthenticationContextElement(
    AuthenticationDetails(
        UUID(0L, 0L),
        "system-admin",
        Roles.entries.filter { it.isSystemRole() },
        active = true,
        email = "system-admin",
        emailVerified = false,
        locale = LocaleBinding.default()
    )
)

public suspend fun <T> withSystemContext(
    block: suspend CoroutineScope.() -> T
): T = withContext(systemContext, block)