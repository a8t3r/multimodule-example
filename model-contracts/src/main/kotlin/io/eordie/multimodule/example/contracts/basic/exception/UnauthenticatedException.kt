package io.eordie.multimodule.example.contracts.basic.exception

import kotlinx.serialization.Serializable

@Serializable
class UnauthenticatedException : SecurityException() {
    override val message = "unauthenticated"
}
