package io.eordie.multimodule.example.rsocket.context

import io.eordie.multimodule.example.contracts.basic.exception.UnauthenticatedException
import io.eordie.multimodule.example.contracts.identitymanagement.models.AuthenticationDetails
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
