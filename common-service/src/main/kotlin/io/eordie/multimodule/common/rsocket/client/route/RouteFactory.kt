package io.eordie.multimodule.common.rsocket.client.route

import io.eordie.multimodule.common.rsocket.client.rsocket.KubernetesRSocketFactory
import io.eordie.multimodule.common.rsocket.client.rsocket.LocalRSocketFactory
import io.eordie.multimodule.common.rsocket.client.rsocket.RSocketLocalFactory
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Secondary

class RouteFactory(private val context: ApplicationContext) {

    private val isSimulateRemoteRouting: Boolean by lazy {
        context.environment.get(
            "micronaut.application.simulate-remote-routing",
            Boolean::class.java,
            false
        )
    }

    private val socketFactory: RSocketLocalFactory by lazy {
        if (isSimulateRemoteRouting) LocalRSocketFactory(context) else KubernetesRSocketFactory(context)
    }

    fun newRoute(serviceInterface: Class<*>): Any {
        val primaryDefinition = context.getBeanDefinitions(serviceInterface)
            .firstOrNull { !it.annotationMetadata.hasDeclaredAnnotation(Secondary::class.java) }

        val invoker = when {
            primaryDefinition == null -> RemoteRoute(serviceInterface.kotlin, context, socketFactory)
            !primaryDefinition.hasDeclaredAnnotation(LocalRouteOnly::class.java) && isSimulateRemoteRouting ->
                RemoteRoute(serviceInterface.kotlin, context, socketFactory)
            else -> LocalRoute(serviceInterface.kotlin, context, context.getBean(primaryDefinition))
        }

        return invoker.proxy()
    }
}
