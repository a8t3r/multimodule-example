package io.eordie.multimodule.common.repository

import com.google.common.base.Defaults
import io.eordie.multimodule.common.repository.entity.PermissionAwareIF
import io.eordie.multimodule.common.utils.GenericTypes
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.utils.getIntrospection
import io.micronaut.core.beans.BeanIntrospection
import kotlinx.serialization.Required
import org.babyfish.jimmer.runtime.ImmutableSpi
import java.time.OffsetDateTime
import java.util.*
import kotlin.reflect.KClass

object EntityConverter {

    private val defaults: Map<Class<out Any>, Any> = mapOf(
        UUID::class.java to UUID.fromString("deadbeef-dead-beef-dead-beef00000075"),
        String::class.java to "dead-beef",
        Set::class.java to emptySet<Any>(),
        List::class.java to emptyList<Any>(),
        OffsetDateTime::class.java to OffsetDateTime.now()
    )

    fun <T : Any> convert(convertable: Convertable<T>): T {
        val introspection = this.getIntrospection(convertable::class)
        return convert(convertable as ImmutableSpi, introspection)
    }

    fun <T : Any, C : Convertable<out T>> getIntrospection(type: KClass<out C>): BeanIntrospection<T> {
        val targetType = GenericTypes.getTypeArgumentFromClass(type, Convertable::class)
        return getIntrospection(targetType)
    }

    private fun <T : Any> convert(
        from: ImmutableSpi,
        introspection: BeanIntrospection<T>
    ): T {
        val immutableType = from.__type()
        val constructorArgs = introspection.constructorArguments.map { argument ->
            val prop = immutableType.getProp(argument.name)
            when {
                from.__isLoaded(prop.id) -> from.__get(prop.id)
                argument.isNullable -> null
                argument.name == PermissionAwareIF::permissions.name -> emptyList<Permission>()
                argument.isAnnotationPresent(Required::class.java) -> {
                    error("required argument '${argument.name}' is not loaded")
                }
                argument.isPrimitive -> Defaults.defaultValue(argument.type)
                else -> defaults[argument.type] ?: run { error("no default value for type ${argument.type}") }
            }
        }
        return introspection.instantiate(*constructorArgs.toTypedArray())
    }
}
