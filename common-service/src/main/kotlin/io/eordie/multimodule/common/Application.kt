package io.eordie.multimodule.common

import io.eordie.multimodule.common.rsocket.client.DefinedServicesProvider
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.Micronaut

const val GENERATED_RESOURCE = "../../../build/resources/main/application.yaml"

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
