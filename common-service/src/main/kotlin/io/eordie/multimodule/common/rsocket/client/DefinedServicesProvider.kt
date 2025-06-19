package io.eordie.multimodule.common.rsocket.client

import io.eordie.multimodule.common.rsocket.client.route.RouteFactory
import io.eordie.multimodule.contracts.Mutation
import io.eordie.multimodule.contracts.Query
import io.eordie.multimodule.contracts.Subscription
import io.eordie.multimodule.contracts.utils.safeCast
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

    private lateinit var routeFactory: RouteFactory

    fun initialize(context: ApplicationContextProvider): Sequence<RuntimeBeanDefinition<*>> {
        return registerClients(context, Query::class.java) +
            registerClients(context, Mutation::class.java) +
            registerClients(context, Subscription::class.java)
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
                val serviceInterface: Class<Any> = safeCast(Class.forName(line))

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
        if (!this::routeFactory.isInitialized) {
            routeFactory = RouteFactory(provider.applicationContext)
        }

        return routeFactory.newRoute(serviceInterface)
    }
}
