package io.eordie.multimodule.contracts.basic.exception

import kotlinx.serialization.Serializable

@Serializable
class UnauthenticatedException : SecurityException() {
    override val message = "unauthenticated"

    override fun createCopy(): BaseRuntimeException {
        val exception = UnauthenticatedException()
        exception.initCause(this)
        return exception
    }
}
