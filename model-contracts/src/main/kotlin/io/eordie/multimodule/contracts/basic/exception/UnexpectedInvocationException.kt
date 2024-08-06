package io.eordie.multimodule.contracts.basic.exception

import kotlinx.serialization.Serializable

@Serializable
class UnexpectedInvocationException(private val definition: ExceptionDefinition) : BaseRuntimeException() {
    override val message: String? get() = definition.message

    override fun createCopy(): BaseRuntimeException {
        val exception = UnexpectedInvocationException(definition)
        exception.initCause(this)
        return exception
    }
}
