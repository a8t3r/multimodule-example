package io.eordie.multimodule.common.rsocket.context

import io.eordie.multimodule.contracts.organization.models.acl.EmployeeAcl
import kotlin.coroutines.CoroutineContext

class AclContextElement : CoroutineContext.Element {

    lateinit var resource: List<EmployeeAcl>

    fun isInitialized() = ::resource.isInitialized

    fun initialize(resource: List<EmployeeAcl>): AclContextElement {
        this.resource = resource
        return this
    }

    fun combine(that: AclContextElement?) {
        if (!this.isInitialized() && that != null && that.isInitialized()) {
            this.resource = that.resource
        }
    }

    companion object Key : CoroutineContext.Key<AclContextElement>

    override val key: CoroutineContext.Key<*> = Key
}

fun CoroutineContext.getOrCreateAclElement() = this[AclContextElement.Key] ?: AclContextElement()
