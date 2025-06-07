package io.eordie.multimodule.contracts.basic.exception

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Introspected
@Serializable
class EntityNotFoundException(
    val entityId: String,
    val entityType: String
) : BaseRuntimeException() {

    constructor(entityId: Any, entityType: KClass<*>) : this(entityId.toString(), requireNotNull(entityType.simpleName))

    override fun extensions(): Map<String, Any> = mapOf(
        "entityId" to entityId,
        "entityType" to entityType
    )
}
