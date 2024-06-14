package io.eordie.multimodule.common.rsocket.context

import io.eordie.multimodule.contracts.basic.exception.UnauthenticatedException
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails
import io.eordie.multimodule.contracts.identitymanagement.models.LocaleBinding
import io.eordie.multimodule.contracts.utils.Roles
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
    return get(AuthenticationContextElement.Key)?.details ?: throw UnauthenticatedException()
}

suspend fun getAuthentication() = coroutineContext.getAuthenticationContext()

val systemContext = AuthenticationContextElement(
    AuthenticationDetails(
        UUID(0L, 0L),
        "system-admin",
        Roles.entries.filter { it.isSystemRole() },
        active = true,
        emailVerified = false,
        locale = LocaleBinding.default()
    )
)
