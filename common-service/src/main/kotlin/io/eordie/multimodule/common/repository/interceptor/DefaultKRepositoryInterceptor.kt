package io.eordie.multimodule.common.repository.interceptor

import io.eordie.multimodule.common.repository.KBaseFactory
import io.eordie.multimodule.common.repository.KRepository
import io.eordie.multimodule.contracts.utils.safeCast
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.BeanLocator
import io.micronaut.context.annotation.Prototype
import io.micronaut.data.annotation.Query
import io.micronaut.inject.qualifiers.Qualifiers
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.functions
import kotlin.reflect.full.superclasses
import kotlin.reflect.javaType
import kotlin.reflect.jvm.javaMethod

@Prototype
@InterceptorBean(KRepository::class)
class DefaultKRepositoryInterceptor(
    private val beanLocator: BeanLocator,
    private val filterInterceptor: FindOneByPredicateInterceptor
) : MethodInterceptor<Any, Any> {

    @OptIn(ExperimentalStdlibApi::class)
    private fun KTypeProjection.asArgument(): Class<*> = requireNotNull(this.type?.javaType) as Class<*>

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        val (entityType, entityId) = context.target::class.superclasses[0].supertypes[0].arguments
        val factoryBean = beanLocator.getBean(
            KBaseFactory::class.java,
            Qualifiers.byTypeArguments(
                entityType.asArgument(),
                Any::class.java,
                Any::class.java,
                entityId.asArgument(),
                Any::class.java
            )
        )

        val method = context.executableMethod
        val factoryMethods = factoryBean::class.functions
            .filter { it.name == method.name && it.parameters.size == method.arguments.size }

        val factoryMethod = when {
            factoryMethods.isEmpty() -> null
            factoryMethods.size == 1 -> factoryMethods[0]
            else -> {
                error("todo: currently dispatch ignores argument types")
            }
        }

        return if (factoryMethod != null) {
            requireNotNull(factoryMethod.javaMethod).invoke(factoryBean, *(context.parameterValues))
        } else if (context.stringValue(Query::class.java).isPresent) {
            filterInterceptor.intercept(factoryBean.sql, safeCast<KClass<Any>>(factoryBean.entityType), context)
        } else {
            error("No query present in method")
        }
    }
}
