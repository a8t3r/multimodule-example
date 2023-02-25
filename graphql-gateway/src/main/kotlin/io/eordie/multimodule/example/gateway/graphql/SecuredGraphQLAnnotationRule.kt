package io.eordie.multimodule.example.gateway.graphql

import graphql.execution.AbortExecutionException
import io.eordie.multimodule.example.contracts.annotations.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.security.token.DefaultRolesFinder
import io.micronaut.security.token.config.TokenConfigurationProperties
import reactor.core.publisher.Mono
import kotlin.reflect.KFunction

class SecuredGraphQLAnnotationRule :
    SecuredAnnotationRule(DefaultRolesFinder(TokenConfigurationProperties())) {

    fun checkRoles(authentication: Authentication?, implFn: KFunction<*>) {
        val findAnnotations = implFn.annotations.filterIsInstance<Secured>()
        if (findAnnotations.isNotEmpty()) {
            val isAllowed = findAnnotations
                .map { isAllowed(it, authentication) }
                .any { it == SecurityRuleResult.ALLOWED }

            if (!isAllowed) {
                val requiredRoles = findAnnotations.flatMap { a -> a.value.map { it.humanName() } }
                val message = "access denied for method ${implFn.name}, requires $requiredRoles"
                throw AbortExecutionException(message)
            }
        }
    }

    private fun isAllowed(secured: Secured, authentication: Authentication?): SecurityRuleResult? {
        return when {
            secured.allowAnonymous -> SecurityRuleResult.ALLOWED
            secured.denyAll -> SecurityRuleResult.REJECTED
            else -> {
                val requiredRoles = secured.value.map { it.humanName() }.toList()
                val comparison = compareRoles(requiredRoles, getRoles(authentication))
                (comparison as Mono<SecurityRuleResult>).block()
            }
        }
    }
}
