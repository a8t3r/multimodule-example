package io.eordie.multimodule.common.jmx

import io.micronaut.configuration.jmx.endpoint.EndpointMBeanFactory
import io.micronaut.context.annotation.Replaces
import io.micronaut.core.convert.ConversionService
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import io.micronaut.management.endpoint.annotation.Write
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.function.Supplier
import javax.management.DynamicMBean

@Singleton
@Named("endpoint")
@Replaces(EndpointMBeanFactory::class)
class CustomEndpointMBeanFactory(
    private val conversion: ConversionService,
    private val mbeanService: MBeanService
) : EndpointMBeanFactory() {

    private val index: MutableMap<String, Descriptor> = mutableMapOf()

    private data class Descriptor(
        val instanceSupplier: Supplier<Any>,
        val methods: List<ExecutableMethod<Any, Any>>
    ) {

        fun findSetter(methodName: String): ExecutableMethod<Any, Any>? =
            methods.singleOrNull { it.name == methodName && it.arguments.size == 1 }

        fun invoke(conversion: ConversionService, methodName: String, value: String?): Boolean {
            val method = findSetter(methodName)
            return if (method == null) false else {
                val transformed = conversion.convert(value, method.argumentTypes.single()).orElseThrow()
                method.invoke(instanceSupplier.get(), transformed)
                true
            }
        }
    }

    private fun BeanDefinition<*>.shortenName() = declaringType.map {
        val packageAlias = it.`package`.name.split(".")
            .fold("") { acc, s -> acc + s[0] + "." }
        packageAlias + it.simpleName
    }.orElseThrow()

    override fun createMBean(
        beanDefinition: BeanDefinition<*>,
        methods: MutableCollection<ExecutableMethod<Any, Any>>,
        instanceSupplier: Supplier<Any>
    ): Any {
        val beanAlias = beanDefinition.shortenName()
        val setters = methods.filter {
            it.hasAnnotation(Write::class.java) && it.arguments.size == 1
        }

        if (setters.isNotEmpty()) {
            index[beanAlias] = Descriptor(instanceSupplier, setters)
            mbeanService.findByName(beanAlias).forEach { bean ->
                val isValidProperty = updateLocalState(beanAlias, bean.id.property, bean.value)
                if (isValidProperty != null && bean.actual != isValidProperty) {
                    mbeanService.save(MBeanModel(bean) { this.actual = isValidProperty })
                }
            }
        }

        val instance = super.createMBean(beanDefinition, methods, instanceSupplier) as DynamicMBean
        return object : DynamicMBean by instance {
            override fun invoke(actionName: String, params: Array<out Any>, signature: Array<out String>): Any? {
                return instance.invoke(actionName, params, signature).also {
                    if (params.size == 1) {
                        updateSharedState(beanAlias, actionName, params[0])
                    }
                }
            }
        }
    }

    private fun updateSharedState(beanName: String, methodName: String, value: Any?) {
        if (beanName in index && index.getValue(beanName).findSetter(methodName) != null) {
            mbeanService.save(
                MBeanModel {
                    this.id = MBeanKey {
                        this.name = beanName
                        this.property = methodName
                    }
                    this.value = conversion.convert(value, String::class.java).orElseThrow()
                }
            )
        }
    }

    internal fun updateLocalState(beanName: String, methodName: String, value: String?): Boolean? {
        return (index[beanName] ?: return null).invoke(conversion, methodName, value)
    }
}
