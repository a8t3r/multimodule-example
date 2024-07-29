package io.eordie.multimodule.common.repository

import com.google.common.base.Defaults
import io.eordie.multimodule.common.repository.entity.PermissionAwareIF
import io.eordie.multimodule.common.utils.GenericTypes
import io.eordie.multimodule.contracts.basic.Permission
import io.eordie.multimodule.contracts.utils.getIntrospection
import io.micronaut.core.beans.BeanIntrospection
import org.babyfish.jimmer.runtime.ImmutableSpi
import java.time.OffsetDateTime

object EntityConverter {

    private val defaults: Map<Class<out Any>, Any> = mapOf(
        String::class.java to "",
        Set::class.java to emptySet<Any>(),
        List::class.java to emptyList<Any>(),
        OffsetDateTime::class.java to OffsetDateTime.now()
    )

    fun <T : Any> convert(convertable: Convertable<T>): T {
        val targetType = GenericTypes.getTypeArgument(convertable, Convertable::class)
        val introspection = getIntrospection<T>(targetType)
        return convert(convertable as ImmutableSpi, introspection)
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
                argument.isPrimitive -> Defaults.defaultValue(argument.type)
                else -> defaults[argument.type] ?: run { error("required argument '${argument.name}' is not loaded") }
            }
        }
        return introspection.instantiate(*constructorArgs.toTypedArray())
    }
}
