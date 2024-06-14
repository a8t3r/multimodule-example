package io.eordie.multimodule.common.rsocket.server

import io.eordie.multimodule.common.utils.like
import io.eordie.multimodule.contracts.annotations.Secured
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

    val serviceFunction: KFunction<*> = serviceInterface.declaredFunctions.like(implementationFunction)

    val securedConstraints: List<Secured> by lazy { serviceFunction.annotations.filterIsInstance<Secured>() }
}
