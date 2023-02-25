package io.eordie.multimodule.example.contracts.basic.exception

import io.micronaut.core.annotation.Introspected
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Introspected
@Serializable
class EntityNotFoundException(
    val entityId: @Contextual Any,
    val entityType: String
) : BaseRuntimeException() {

    constructor(entityId: Any, entityType: KClass<*>) : this(entityId, requireNotNull(entityType.simpleName))

    override val extensions: Map<String, @Contextual Any> = mapOf(
        "entityId" to entityId,
        "entityType" to entityType
    )
}
