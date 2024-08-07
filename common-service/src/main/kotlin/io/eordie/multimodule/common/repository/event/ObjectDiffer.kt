package io.eordie.multimodule.common.repository.event

import io.eordie.multimodule.common.repository.entity.VersionedEntityIF
import io.eordie.multimodule.contracts.AuditLog
import io.eordie.multimodule.contracts.basic.event.Difference
import io.micronaut.core.beans.BeanIntrospection
import io.micronaut.core.beans.BeanIntrospection.getIntrospection

object ObjectDiffer {

    private val auditLogProperties = getIntrospection(AuditLog::class.java).propertyNames.toSet() + setOf(
        VersionedEntityIF::version.name
    )

    fun <T : Any> difference(oldValue: T?, newValue: T?): Difference? {
        return if (oldValue == null || newValue == null) null else {
            val introspection = getIntrospection(newValue::class.java) as BeanIntrospection<T>
            introspection.beanProperties
                .filterNot { it.name in auditLogProperties }
                .fold(Difference()) { acc, property ->
                    val (old, new) = property.get(oldValue) to property.get(newValue)
                    val differ = when {
                        old == null && new != null -> acc.set
                        old != null && new == null -> acc.unset
                        old != new -> acc.updated
                        else -> null
                    }
                    differ?.add(property.name)
                    acc
                }
        }
    }
}
