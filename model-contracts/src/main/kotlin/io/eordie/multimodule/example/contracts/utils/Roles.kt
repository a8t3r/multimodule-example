package io.eordie.multimodule.example.contracts.utils

import kotlinx.serialization.Serializable

@Serializable
enum class Roles {
    MANAGE_ORGANIZATIONS,
    VIEW_ORGANIZATIONS,
    VIEW_ORGANIZATION,
    MANAGE_ORGANIZATION,
    VIEW_MEMBERS,
    MANAGE_MEMBERS,
    MANAGE_USERS,
    CREATE_ORGANIZATION
    ;

    fun humanName(): String = name.lowercase().replace('_', '-')

    companion object {
        fun supportedFrom(roles: Collection<String>): List<Roles> {
            return Roles.entries.filter { it.humanName() in roles }
        }
    }
}
