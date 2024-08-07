package io.eordie.multimodule.contracts.basic

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import kotlinx.serialization.Serializable

interface Permission {
    val name: String
}

@Serializable
enum class BasePermission : Permission {
    VIEW,
    MANAGE,
    PURGE
}

@GraphQLIgnore
interface PermissionAware<T> where T : Permission, T : Enum<T> {
    val permissions: List<T>
}
