package io.eordie.multimodule.example.gateway.graphql

import com.expediagroup.graphql.generator.execution.FunctionDataFetcher
import graphql.execution.AbortExecutionException
import graphql.schema.DataFetchingEnvironment
import io.micronaut.http.HttpAttributes
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecuredAnnotationRule
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.security.token.DefaultRolesFinder
import io.micronaut.security.token.config.TokenConfigurationProperties
import reactor.core.publisher.Mono
import kotlin.reflect.KFunction

class SecuredFunctionDataFetcher(
    private val target: Any?,
    private val fn: KFunction<*>
) : FunctionDataFetcher(target, fn) {

    private class SecuredGraphQLAnnotationRule :
        SecuredAnnotationRule(DefaultRolesFinder(TokenConfigurationProperties())) {
        fun isAllowed(secured: Secured, authentication: Authentication?): SecurityRuleResult? {
            val requiredRoles = secured.value.toList()
            if (requiredRoles.contains(DENY_ALL)) {
                return SecurityRuleResult.REJECTED
            }

            val comparison = compareRoles(requiredRoles, getRoles(authentication))
            return (comparison as Mono<SecurityRuleResult>).block()
        }
    }

    companion object {
        private val rule = SecuredGraphQLAnnotationRule()
    }

    override fun get(environment: DataFetchingEnvironment): Any? {
        return if (target == null) super.get(environment) else {
            val authentication = environment.graphQlContext.get<Authentication>(HttpAttributes.PRINCIPAL)
            val findAnnotations = fn.findAnnotations<Secured>(target::class)
            val isAllowed = if (findAnnotations.isNotEmpty()) {
                findAnnotations.map { rule.isAllowed(it, authentication) }.any { it == SecurityRuleResult.ALLOWED }
            } else {
                authentication != null
            }

            if (isAllowed) super.get(environment) else {
                throw AbortExecutionException("access denied for method ${fn.name}")
            }
        }
    }
}
