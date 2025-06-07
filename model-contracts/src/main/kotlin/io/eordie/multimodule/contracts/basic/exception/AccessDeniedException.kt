package io.eordie.multimodule.contracts.basic.exception

import io.eordie.multimodule.contracts.utils.Roles
import io.eordie.multimodule.contracts.utils.UuidStr
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class AccessDeniedException(
    private val subjectUserId: UuidStr?,
    private val missingRoles: Collection<Roles>
) : SecurityException() {
    constructor(subjectUserId: UUID?, missingRole: Roles) : this(subjectUserId, setOf(missingRole))

    override fun extensions(): Map<String, Any> = listOfNotNull(
        subjectUserId?.let { "subjectUserId" to it },
        "missingRoles" to missingRoles
    ).toMap()

    override val message by lazy {
        if (missingRoles.isNotEmpty()) {
            "access denied for $subjectUserId, missing roles $missingRoles"
        } else {
            "forbidden method"
        }
    }
}
