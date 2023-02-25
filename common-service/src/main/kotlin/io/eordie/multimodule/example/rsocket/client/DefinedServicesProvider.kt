package io.eordie.multimodule.example.rsocket.client

import io.eordie.multimodule.example.contracts.Mutation
import io.eordie.multimodule.example.contracts.Query
import io.eordie.multimodule.example.rsocket.client.invocation.LocalRoute
import io.eordie.multimodule.example.rsocket.client.invocation.RemoteRoute
import io.micronaut.context.ApplicationContextProvider
import io.micronaut.context.RuntimeBeanDefinition
import io.micronaut.context.annotation.Secondary
import io.micronaut.core.io.ResourceResolver
import io.micronaut.inject.annotation.MutableAnnotationMetadata
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.streams.asSequence

class DefinedServicesProvider {

    companion object {
        private val secondaryAnnotationMetadata = MutableAnnotationMetadata().apply {
            addDeclaredAnnotation(Secondary::class.java.canonicalName, emptyMap())
        }
    }

    fun initialize(context: ApplicationContextProvider): Sequence<RuntimeBeanDefinition<*>> {
        return registerClients(context, Query::class.java) + registerClients(context, Mutation::class.java)
    }

    private fun <T : Any> registerClients(
        provider: ApplicationContextProvider,
        type: Class<T>
    ): Sequence<RuntimeBeanDefinition<*>> {
        val stream: InputStream = ResourceResolver()
            .getResourceAsStream("classpath:META-INF/services/${type.canonicalName}")
            .orElseThrow()

        return sequence {
            val reader = BufferedReader(InputStreamReader(stream))
            reader.lines().asSequence().forEach { line ->
                @Suppress("UNCHECKED_CAST")
                val serviceInterface: Class<Any> = Class.forName(line) as Class<Any>

                yield(
                    // apply the lowest priority during candidate bean picking
                    RuntimeBeanDefinition.builder(serviceInterface) {
                        buildProxyInstance(provider, serviceInterface)
                    }
                        .annotationMetadata(secondaryAnnotationMetadata)
                        .singleton(true)
                        .build()
                )
            }
            reader.close()
        }
    }

    private fun buildProxyInstance(
        provider: ApplicationContextProvider,
        serviceInterface: Class<Any>
    ): Any {
        val context = provider.applicationContext
        val primaryDefinition = context.getBeanDefinitions(serviceInterface)
            .firstOrNull { !it.annotationMetadata.hasDeclaredAnnotation(Secondary::class.java) }

        val invoker = if (primaryDefinition != null) {
            val primaryBean = context.getBean(primaryDefinition)
            LocalRoute(serviceInterface.kotlin, context, primaryBean)
        } else {
            RemoteRoute(serviceInterface.kotlin, context)
        }

        return invoker.proxy()
    }
}
