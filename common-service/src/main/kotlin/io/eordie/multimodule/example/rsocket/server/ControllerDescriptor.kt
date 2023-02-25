package io.eordie.multimodule.example.rsocket.server

import io.eordie.multimodule.example.contracts.annotations.Secured
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions

data class ControllerDescriptor(
    val implementation: Any,
    val serviceInterface: KClass<*>,
    val implementationFunction: KFunction<*>,
    val namePrefix: String = ""
) {
    val name: String = "$namePrefix${serviceInterface.simpleName}:${implementationFunction.name}${implementationFunction.parameters.size}"

    val serviceFunction: KFunction<*> = serviceInterface.declaredFunctions
        .first { it.name == implementationFunction.name && it.parameters.size == implementationFunction.parameters.size }

    val securedConstraints: List<Secured> by lazy { serviceFunction.annotations.filterIsInstance<Secured>() }
}
