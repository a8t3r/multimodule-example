package io.eordie.multimodule.common.rsocket.client.route

import io.eordie.multimodule.contracts.annotations.Secured
import io.eordie.multimodule.contracts.basic.exception.AccessDeniedException
import io.eordie.multimodule.contracts.basic.exception.SecurityException
import io.eordie.multimodule.contracts.basic.exception.UnauthenticatedException
import io.eordie.multimodule.contracts.identitymanagement.models.AuthenticationDetails

object AuthorizationCheck {
    fun check(
        classSecured: Secured?,
        securedAnnotations: List<Secured>,
        auth: AuthenticationDetails?
    ): SecurityException? {
        return if (classSecured == null && securedAnnotations.isEmpty()) {
            if (auth != null) null else UnauthenticatedException()
        } else {
            classSecured?.let { check(it, auth) } ?: securedAnnotations.firstNotNullOfOrNull { check(it, auth) }
        }
    }

    private fun check(secured: Secured, auth: AuthenticationDetails?): SecurityException? {
        return when {
            secured.allowAnonymous -> null
            secured.denyAll -> AccessDeniedException(auth?.userId, emptySet())
            auth == null -> UnauthenticatedException()
            else -> {
                val presentRoles = auth.roles.toSet()
                var validation: AccessDeniedException? = null
                if (secured.oneOf.isNotEmpty()) {
                    val requiredRoles = secured.oneOf.toSet()
                    if (requiredRoles.intersect(presentRoles).isEmpty()) {
                        validation = AccessDeniedException(auth.userId, requiredRoles)
                    }
                }

                if (secured.allOf.isNotEmpty() || secured.value.isNotEmpty()) {
                    val requiredRoles = secured.value.takeIf { it.isNotEmpty() } ?: secured.allOf
                    val missingRoles = requiredRoles.toSet().subtract(presentRoles)
                    if (missingRoles.isNotEmpty()) {
                        validation = AccessDeniedException(auth.userId, missingRoles)
                    }
                }

                validation
            }
        }
    }
}
