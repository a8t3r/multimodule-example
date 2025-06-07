package io.eordie.multimodule.contracts.basic.exception

import kotlinx.serialization.Serializable

@Serializable
class UnexpectedInvocationException(
    override val message: String?,
    val className: String?
) : BaseRuntimeException()
