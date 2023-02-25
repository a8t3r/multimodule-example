package io.eordie.multimodule.example

import io.eordie.multimodule.example.rsocket.client.DefinedServicesProvider
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.Micronaut

fun runApplication(args: Array<String>): ApplicationContext {
    return SyntheticSupport()
        .args(*args)
        .allowEmptyProviders(true)
        .eagerInitSingletons(true)
        .eagerInitConfiguration(true)
        .start()
}

class SyntheticSupport : Micronaut() {
    override fun newApplicationContext(): ApplicationContext {
        val context = super.newApplicationContext()
        DefinedServicesProvider()
            .initialize { context }
            .forEach {
                context.registerBeanDefinition(it)
            }

        return context
    }
}
